const API_BASE = 'http://localhost:8080/api';
let currentUser = null; // Stores the full user object (name, username, etc.)

// --- NAVIGATION LOGIC ---

function toggleAuth(view) {
    document.getElementById('login-form').style.display = view === 'login' ? 'block' : 'none';
    document.getElementById('register-form').style.display = view === 'register' ? 'block' : 'none';
    document.getElementById('auth-message').textContent = '';
}

function showSection(sectionId) {
    // Hide all dashboard sections
    document.getElementById('dashboard-menu').style.display = 'none';
    document.getElementById('save-section').style.display = 'none';
    document.getElementById('show-section').style.display = 'none';
    document.getElementById('generate-section').style.display = 'none';

    // Reset the 'Show Passwords' state when leaving it
    if (sectionId !== 'show-section') {
        document.getElementById('password-list-container').style.display = 'none';
        document.getElementById('key-verification').style.display = 'block';
        document.getElementById('verify-secretkey').value = '';
    }

    // Show the requested section
    document.getElementById(sectionId).style.display = 'block';
}

// --- AUTHENTICATION ---

async function register() {
    const user = {
        name: document.getElementById('reg-name').value,
        username: document.getElementById('reg-username').value,
        email: document.getElementById('reg-email').value,
        gender: document.getElementById('reg-gender').value,
        dob: document.getElementById('reg-dob').value,
        secretkey: document.getElementById('reg-secretkey').value
    };

    try {
        const response = await fetch(`${API_BASE}/users/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(user)
        });

        if (response.ok) {
            document.getElementById('auth-message').textContent = "Registration successful! Please login.";
            document.getElementById('auth-message').style.color = "green";
            toggleAuth('login');
        } else {
            document.getElementById('auth-message').textContent = "Registration failed. Username might be taken.";
            document.getElementById('auth-message').style.color = "red";
        }
    } catch (error) {
        console.error("Error:", error);
    }
}

async function login() {
    const username = document.getElementById('login-username').value;
    const secretkey = document.getElementById('login-secretkey').value;

    try {
        const response = await fetch(`${API_BASE}/users/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, secretkey })
        });

        if (response.ok) {
            currentUser = await response.json(); // Save full user details

            document.getElementById('welcome-message').textContent = `Hey ${currentUser.name}, welcome to the Password Manager`;
            document.getElementById('auth-container').style.display = 'none';
            document.getElementById('dashboard-container').style.display = 'block';
            showSection('dashboard-menu');
        } else {
            document.getElementById('auth-message').textContent = "Invalid username or secret key.";
            document.getElementById('auth-message').style.color = "red";
        }
    } catch (error) {
        console.error("Error:", error);
    }
}

function logout() {
    currentUser = null;
    document.getElementById('auth-container').style.display = 'block';
    document.getElementById('dashboard-container').style.display = 'none';

    // Clear all inputs
    document.querySelectorAll('input').forEach(input => input.value = '');
    document.getElementById('generated-result').style.display = 'none';
}

// --- OPTION 1: SAVE NEW PASSWORD ---

function checkStrength() {
    const pwd = document.getElementById('save-pwd').value;
    const strengthText = document.getElementById('strength-text');

    if (pwd.length === 0) {
        strengthText.textContent = "None";
        strengthText.style.color = "black";
        return;
    }

    let strength = 0;
    if (pwd.length > 7) strength++;
    if (/[A-Z]/.test(pwd)) strength++;
    if (/[0-9]/.test(pwd)) strength++;
    if (/[^A-Za-z0-9]/.test(pwd)) strength++;

    if (strength <= 1) {
        strengthText.textContent = "Weak";
        strengthText.style.color = "red";
    } else if (strength === 2 || strength === 3) {
        strengthText.textContent = "Medium";
        strengthText.style.color = "orange";
    } else {
        strengthText.textContent = "Strong";
        strengthText.style.color = "green";
    }
}

async function savePassword() {
    const passwordName = document.getElementById('save-name').value;
    const encryptedPassword = document.getElementById('save-pwd').value; // Backend handles encryption

    try {
        const response = await fetch(`${API_BASE}/passwords/${currentUser.username}/save`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ passwordName, encryptedPassword })
        });

        if (response.ok) {
            alert("Password encrypted and saved securely!");
            document.getElementById('save-name').value = '';
            document.getElementById('save-pwd').value = '';
            document.getElementById('strength-text').textContent = 'None';
            showSection('dashboard-menu');
        }
    } catch (error) {
        console.error("Error:", error);
    }
}

// --- OPTION 2: SHOW SAVED PASSWORDS (10-Second Timeout Logic) ---

async function verifyKeyAndLoadPasswords() {
    const enteredKey = document.getElementById('verify-secretkey').value;

    // Verify against the user object stored during login
    if (enteredKey === currentUser.secretkey) {
        document.getElementById('key-verification').style.display = 'none';
        document.getElementById('password-list-container').style.display = 'block';
        await loadPasswords();
    } else {
        alert("Incorrect Secret Key!");
    }
}

async function loadPasswords() {
    try {
        const response = await fetch(`${API_BASE}/passwords/${currentUser.username}/all`);
        const passwords = await response.json();

        const tableBody = document.getElementById('password-list');
        tableBody.innerHTML = '';

        passwords.forEach(entry => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${entry.passwordName}</td>
                <td id="pwd-display-${entry.id}">********</td>
                <td>
                    <button class="eye-btn" onclick="revealPassword(${entry.id})">👁️ Show</button>
                </td>
            `;
            tableBody.appendChild(row);
        });
    } catch (error) {
        console.error("Error fetching passwords:", error);
    }
}

let timeoutTimers = {}; // Keep track of timers to clear them if clicked again

async function revealPassword(entryId) {
    const displayCell = document.getElementById(`pwd-display-${entryId}`);
    const providedKey = document.getElementById('verify-secretkey').value;

    // Clear any existing timer for this specific password
    if (timeoutTimers[entryId]) {
        clearTimeout(timeoutTimers[entryId]);
    }

    try {
        // Request decryption from the backend
        const response = await fetch(`${API_BASE}/passwords/${currentUser.username}/decrypt/${entryId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(providedKey)
        });

        if (response.ok) {
            const plainText = await response.text();
            displayCell.textContent = plainText;

            // Set 10-second timeout to hide it again
            timeoutTimers[entryId] = setTimeout(() => {
                displayCell.textContent = '********';
            }, 10000); // 10,000 milliseconds = 10 seconds

        } else {
            alert("Failed to decrypt.");
        }
    } catch (error) {
        console.error("Error:", error);
    }
}

// --- OPTION 3: GENERATE PASSWORD ---

function generatePassword() {
    const lengthInput = document.getElementById('gen-length').value;
    const length = lengthInput ? parseInt(lengthInput) : 12; // Default to 12 if empty

    if (length < 4 || length > 128) {
        alert("Please choose a length between 4 and 128.");
        return;
    }

    const charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()_+~`|}{[]:;?><,./-=";
    let retVal = "";
    for (let i = 0, n = charset.length;i < length;++i) {
        retVal += charset.charAt(Math.floor(Math.random() * n));
    }

    document.getElementById('gen-output').textContent = retVal;
    document.getElementById('generated-result').style.display = 'block';
}

async function saveGeneratedPassword() {
    const passwordName = document.getElementById('gen-name').value;
    const encryptedPassword = document.getElementById('gen-output').textContent;

    if (!passwordName) {
        alert("Please provide a name for this password (e.g., Reddit) before saving.");
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/passwords/${currentUser.username}/save`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ passwordName, encryptedPassword })
        });

        if (response.ok) {
            alert("Generated password added to vault!");
            document.getElementById('gen-name').value = '';
            document.getElementById('gen-length').value = '';
            document.getElementById('generated-result').style.display = 'none';
            showSection('dashboard-menu');
        }
    } catch (error) {
        console.error("Error:", error);
    }
}
// --- NEW: Clear Generated Password ---
function clearGeneratedPassword() {
    // Clear the input fields
    document.getElementById('gen-name').value = '';
    document.getElementById('gen-length').value = '';

    // Clear the generated text
    document.getElementById('gen-output').textContent = '';

    // Hide the result container entirely
    document.getElementById('generated-result').style.display = 'none';
}

function togglePassword(button) {

    const passwordSpan =
        button.closest("tr")
            .querySelector(".password-value");

    const actualPassword =
        passwordSpan.dataset.password;

    const isHidden =
        passwordSpan.dataset.hidden === "true";

    if (isHidden) {

        // Show
        passwordSpan.textContent = actualPassword;
        passwordSpan.dataset.hidden = "false";

        button.textContent = "Hide";

    } else {

        // Hide
        passwordSpan.textContent =
            "•".repeat(actualPassword.length);

        passwordSpan.dataset.hidden = "true";

        button.textContent = "Show";
    }
}