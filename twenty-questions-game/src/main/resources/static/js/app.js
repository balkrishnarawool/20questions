const API_BASE = 'http://localhost:8081/api/game';

let currentSession = null;

// DOM Elements
const startScreen = document.getElementById('startScreen');
const thinkingScreen = document.getElementById('thinkingScreen');
const gameScreen = document.getElementById('gameScreen');
const resultScreen = document.getElementById('resultScreen');

const startBtn = document.getElementById('startBtn');
const readyBtn = document.getElementById('readyBtn');
const playAgainBtn = document.getElementById('playAgainBtn');
const answerButtons = document.querySelectorAll('.answer-buttons .btn');

const questionMain = document.getElementById('questionMain');
const questionContext = document.getElementById('questionContext');
const questionNumber = document.getElementById('questionNumber');
const remainingQuestions = document.getElementById('remainingQuestions');
const progressBar = document.getElementById('progressBar');
const typingIndicator = document.getElementById('typingIndicator');
const historyList = document.getElementById('historyList');

const resultIcon = document.getElementById('resultIcon');
const resultTitle = document.getElementById('resultTitle');
const resultMessage = document.getElementById('resultMessage');

// Screen Navigation
function showScreen(screen) {
    [startScreen, thinkingScreen, gameScreen, resultScreen].forEach(s => {
        s.classList.remove('active');
    });
    screen.classList.add('active');
}

// API Calls
async function startGame() {
    try {
        const response = await fetch(`${API_BASE}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            throw new Error('Failed to start game');
        }
        
        const data = await response.json();
        currentSession = data;
        return data;
    } catch (error) {
        console.error('Error starting game:', error);
        alert('Failed to start game. Make sure the server is running.');
        throw error;
    }
}

async function getFirstQuestion() {
    try {
        typingIndicator.classList.add('active');
        
        const response = await fetch(`${API_BASE}/${currentSession.sessionId}/ready`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.error || 'Failed to get question');
        }
        
        const data = await response.json();
        currentSession = data;
        
        typingIndicator.classList.remove('active');
        return data;
    } catch (error) {
        console.error('Error getting question:', error);
        typingIndicator.classList.remove('active');
        alert(error.message || 'Failed to communicate with server. Make sure Ollama is running.');
        throw error;
    }
}

async function sendAnswer(answer) {
    try {
        typingIndicator.classList.add('active');
        
        const response = await fetch(`${API_BASE}/${currentSession.sessionId}/answer`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                answer: answer
            })
        });
        
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.error || 'Failed to send answer');
        }
        
        const data = await response.json();
        currentSession = data;
        
        // Add slight delay for better UX
        await new Promise(resolve => setTimeout(resolve, 500));
        typingIndicator.classList.remove('active');
        
        return data;
    } catch (error) {
        console.error('Error sending answer:', error);
        typingIndicator.classList.remove('active');
        alert(error.message || 'Failed to communicate with server');
        throw error;
    }
}

// UI Updates
function updateGameUI(data) {
    if (data.questionNumber) {
        questionNumber.textContent = data.questionNumber;
    }
    
    if (data.remainingQuestions !== undefined) {
        remainingQuestions.textContent = `${data.remainingQuestions} remaining`;
    }
    
    const progress = ((data.questionNumber || 0) / 20) * 100;
    progressBar.style.width = `${progress}%`;
    
    if (data.question) {
        questionMain.textContent = data.question;
        // Display context only for questions after the first one
        if (data.questionNumber > 1 && data.context) {
            questionContext.textContent = data.context;
        } else {
            questionContext.textContent = '';
        }
    }

    updateHistory(data.history);
}

function updateHistory(history) {
    if (!history || history.length === 0) {
        historyList.innerHTML = '<p class="history-empty">No questions answered yet</p>';
        return;
    }

    // Filter to only show answered questions
    const answeredQuestions = history.filter(item => item.answer);
    
    if (answeredQuestions.length === 0) {
        historyList.innerHTML = '<p class="history-empty">No questions answered yet</p>';
        return;
    }

    historyList.innerHTML = '';
    // Use forEach on a reversed copy to add items in reverse order
    answeredQuestions.slice().reverse().forEach((item, index) => {
        const historyItem = document.createElement('div');
        historyItem.className = 'history-item';

        const questionDiv = document.createElement('div');
        questionDiv.className = 'history-question';
        questionDiv.innerHTML = `
            <span class="history-question-number">Q${answeredQuestions.length - index}</span>
            <span>${item.question}</span>
        `;

        const answerDiv = document.createElement('div');
        answerDiv.className = `history-answer ${item.answer.toLowerCase()}`;
        answerDiv.textContent = `Answer: ${item.answer}`;

        historyItem.appendChild(questionDiv);
        historyItem.appendChild(answerDiv);
        historyList.appendChild(historyItem);
    });

    // Scroll to bottom
    historyList.scrollTop = historyList.scrollHeight;
}

function showResult(data) {
    if (data.status === 'GUESSED') {
        resultIcon.textContent = '🎉';
        resultTitle.textContent = "I got it!";
        resultMessage.textContent = data.message || `Your person in mind is ${data.guess}`;
    } else if (data.status === 'PLAYER_WON') {
        resultIcon.textContent = '🏆';
        resultTitle.textContent = "You win!";
        resultMessage.textContent = "I couldn't guess your person in 20 questions. Well played!";
    }
    
    showScreen(resultScreen);
}

// Event Handlers
startBtn.addEventListener('click', async () => {
    startBtn.disabled = true;
    startBtn.textContent = 'Starting...';
    
    try {
        await startGame();
        showScreen(thinkingScreen);
        startBtn.disabled = false;
        startBtn.textContent = 'Start Game';
    } catch (error) {
        startBtn.disabled = false;
        startBtn.textContent = 'Start Game';
    }
});

readyBtn.addEventListener('click', async () => {
    readyBtn.disabled = true;
    readyBtn.textContent = 'Loading...';
    showScreen(gameScreen);
    typingIndicator.classList.add('active');
    
    // Disable answer buttons while loading
    answerButtons.forEach(btn => btn.disabled = true);
    
    try {
        const data = await getFirstQuestion();
        if (data && data.question) {
            updateGameUI(data);
            // Enable answer buttons after question is loaded
            answerButtons.forEach(btn => btn.disabled = false);
        }
    } catch (error) {
        showScreen(thinkingScreen);
    } finally {
        typingIndicator.classList.remove('active');
        readyBtn.disabled = false;
        readyBtn.textContent = "I'm Ready!";
    }
});

answerButtons.forEach(button => {
    button.addEventListener('click', async (e) => {
        const answer = e.target.dataset.answer.toUpperCase();
        
        // Disable buttons during processing
        answerButtons.forEach(btn => btn.disabled = true);
        
        // Show typing indicator
        typingIndicator.classList.add('active');
        
        try {
            const data = await sendAnswer(answer);
            
            if (data.status === 'GUESSED' || data.status === 'PLAYER_WON') {
                showResult(data);
            } else if (data.question) {
                updateGameUI(data);
                // Re-enable buttons for next question
                answerButtons.forEach(btn => btn.disabled = false);
            }
        } catch (error) {
            console.error('Error:', error);
            // Re-enable buttons on error
            answerButtons.forEach(btn => btn.disabled = false);
        } finally {
            typingIndicator.classList.remove('active');
        }
    });
});

playAgainBtn.addEventListener('click', () => {
    currentSession = null;
    questionMain.textContent = 'Loading...';
    questionContext.textContent = '';
    questionNumber.textContent = '1';
    remainingQuestions.textContent = '19 remaining';
    progressBar.style.width = '0%';
    historyList.innerHTML = '<p class="history-empty">No questions answered yet</p>';
    startBtn.disabled = false;
    startBtn.textContent = 'Start Game';
    showScreen(startScreen);
});

// Initialize
showScreen(startScreen);
