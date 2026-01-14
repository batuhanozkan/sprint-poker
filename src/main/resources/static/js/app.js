let stompClient = null;
let currentPlayerId = null;
let currentRoomId = null;
let currentPlayerName = null;
let isHost = false;
let votingRevealed = false;

const cards = ["0", "1", "2", "3", "5", "8", "13", "21", "34", "55", "89", "?", "☕"];

// Initialize
document.addEventListener('DOMContentLoaded', function() {
    console.log('Sayfa yüklendi');
    setupEventListeners();
    renderCards();
});

function setupEventListeners() {
    document.getElementById('create-room-btn').onclick = createRoom;
    document.getElementById('join-room-btn').onclick = showJoinForm;
    document.getElementById('confirm-join-btn').onclick = joinRoom;
    document.getElementById('leave-room-btn').onclick = leaveRoom;
    document.getElementById('reveal-btn').onclick = revealVotes;
    document.getElementById('reset-btn').onclick = resetVoting;
    document.getElementById('set-story-btn').onclick = setStory;
    console.log('Event listener\'lar eklendi');
}

async function createRoom() {
    console.log('createRoom çağrıldı');
    
    const playerName = document.getElementById('player-name').value.trim();
    if (!playerName) {
        alert('Lütfen isminizi girin!');
        return;
    }
    
    const roomName = prompt('Oda adını girin:', 'Sprint Planning');
    if (!roomName || roomName.trim() === '') {
        return;
    }
    
    try {
        // REST API ile oda oluştur
        const response = await fetch('/api/room/create', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ roomName: roomName.trim(), playerName: playerName })
        });
        
        const data = await response.json();
        console.log('Oda oluşturuldu:', data);
        
        if (data.error) {
            alert('Hata: ' + data.error);
            return;
        }
        
        currentRoomId = data.roomId;
        currentPlayerId = data.playerId;
        currentPlayerName = playerName;
        isHost = data.isHost;
        
        // WebSocket'e bağlan
        connectWebSocket(function() {
            // Oda bilgilerini al ve ekranı güncelle
            fetchRoomData();
        });
        
    } catch (error) {
        console.error('Oda oluşturma hatası:', error);
        alert('Oda oluşturulamadı: ' + error.message);
    }
}

async function joinRoom() {
    console.log('joinRoom çağrıldı');
    
    const playerName = document.getElementById('player-name').value.trim();
    const roomId = document.getElementById('room-id-input').value.trim();
    
    if (!playerName || !roomId) {
        alert('Lütfen tüm alanları doldurun!');
        return;
    }
    
    try {
        // REST API ile odaya katıl
        const response = await fetch('/api/room/join', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ roomId: roomId, playerName: playerName })
        });
        
        const data = await response.json();
        console.log('Odaya katılındı:', data);
        
        if (data.error) {
            alert('Hata: ' + data.error);
            return;
        }
        
        currentRoomId = data.roomId;
        currentPlayerId = data.playerId;
        currentPlayerName = playerName;
        isHost = data.isHost;
        
        // WebSocket'e bağlan
        connectWebSocket(function() {
            // Diğer oyunculara bildir
            sendPlayerJoined();
            // Oda bilgilerini al ve ekranı güncelle
            fetchRoomData();
        });
        
    } catch (error) {
        console.error('Odaya katılma hatası:', error);
        alert('Odaya katılınamadı: ' + error.message);
    }
}

function connectWebSocket(callback) {
    try {
        const socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        stompClient.debug = null; // Debug'u kapat
        
        stompClient.connect({}, function(frame) {
            console.log('WebSocket bağlantısı kuruldu');
            
            // Odaya subscribe ol
            stompClient.subscribe('/topic/room/' + currentRoomId, function(message) {
                handleMessage(JSON.parse(message.body));
            });
            
            if (callback) callback();
            
        }, function(error) {
            console.error('WebSocket hatası:', error);
        });
    } catch (error) {
        console.error('WebSocket bağlantı hatası:', error);
    }
}

async function fetchRoomData() {
    try {
        const response = await fetch('/api/room/' + currentRoomId);
        const data = await response.json();
        console.log('Oda verileri:', data);
        updateRoomUI(data);
        showGameScreen();
    } catch (error) {
        console.error('Oda verileri alınamadı:', error);
    }
}

function sendPlayerJoined() {
    if (stompClient && stompClient.connected) {
        stompClient.send('/app/room/join', {}, JSON.stringify({
            roomId: currentRoomId,
            playerId: currentPlayerId,
            playerName: currentPlayerName
        }));
    }
}

function showJoinForm() {
    const playerName = document.getElementById('player-name').value.trim();
    if (!playerName) {
        alert('Lütfen isminizi girin!');
        return;
    }
    document.getElementById('join-form').classList.remove('hidden');
}

function leaveRoom() {
    if (confirm('Odadan çıkmak istediğinize emin misiniz?')) {
        if (stompClient && stompClient.connected) {
            stompClient.send('/app/room/leave', {}, JSON.stringify({
                playerId: currentPlayerId
            }));
            stompClient.disconnect();
        }
        showWelcomeScreen();
    }
}

function vote(cardValue) {
    if (votingRevealed) return;
    
    if (stompClient && stompClient.connected) {
        stompClient.send('/app/vote', {}, JSON.stringify({
            playerId: currentPlayerId,
            vote: cardValue
        }));
    }
    
    // UI'ı hemen güncelle
    document.querySelectorAll('.card').forEach(card => {
        card.classList.remove('selected');
        if (card.textContent === cardValue) {
            card.classList.add('selected');
        }
    });
    
    document.getElementById('selected-card').classList.remove('hidden');
    document.getElementById('selected-card-value').textContent = cardValue;
}

function revealVotes() {
    if (!isHost) return;
    
    if (stompClient && stompClient.connected) {
        stompClient.send('/app/reveal', {}, JSON.stringify({
            roomId: currentRoomId,
            playerId: currentPlayerId
        }));
    }
}

function resetVoting() {
    if (!isHost) return;
    
    if (stompClient && stompClient.connected) {
        stompClient.send('/app/reset', {}, JSON.stringify({
            roomId: currentRoomId,
            playerId: currentPlayerId,
            story: ''
        }));
    }
}

function setStory() {
    const story = document.getElementById('story-input').value.trim();
    if (!story) {
        alert('Lütfen story girin!');
        return;
    }
    
    if (stompClient && stompClient.connected) {
        stompClient.send('/app/reset', {}, JSON.stringify({
            roomId: currentRoomId,
            playerId: currentPlayerId,
            story: story
        }));
    }
}

function handleMessage(message) {
    console.log('Mesaj alındı:', message);
    
    if (message.type === 'ERROR') {
        alert('Hata: ' + message.data);
        return;
    }
    
    if (message.data) {
        if (message.type === 'REVEAL_VOTES') {
            votingRevealed = true;
            updateRoomUI(message.data);
            showResults(message.data);
        } else if (message.type === 'RESET_VOTING') {
            votingRevealed = false;
            updateRoomUI(message.data);
            resetUI();
        } else {
            updateRoomUI(message.data);
        }
    }
}

function updateRoomUI(data) {
    const room = data.room;
    let players = data.players;
    
    // Players'ı array'e çevir
    if (players && !Array.isArray(players)) {
        if (typeof players === 'object') {
            // Object ise values'ları al
            players = Object.values(players);
        } else {
            players = [];
        }
    }
    
    if (!room) {
        console.error('Room data yok:', data);
        return;
    }
    
    console.log('Room güncelleniyor:', { room, playerCount: players.length, players });
    
    document.getElementById('room-name').textContent = room.name || 'Oda';
    document.getElementById('room-id-display').textContent = room.id || '';
    document.getElementById('player-count').textContent = players ? players.length : 0;
    
    if (players && players.length > 0) {
        renderPlayers(players);
    } else {
        document.getElementById('players-list').innerHTML = '<p style="color: #94a3b8;">Henüz oyuncu yok</p>';
    }
    
    if (room.currentStory) {
        document.getElementById('story-text').textContent = room.currentStory;
        document.getElementById('story-text').style.color = '#e2e8f0';
        document.getElementById('story-text').style.fontStyle = 'normal';
    } else {
        document.getElementById('story-text').textContent = 'Henüz story girilmedi...';
        document.getElementById('story-text').style.color = '#94a3b8';
        document.getElementById('story-text').style.fontStyle = 'italic';
    }
    
    // Host kontrollerini göster/gizle
    if (isHost) {
        document.getElementById('story-input-section').classList.remove('hidden');
        if (data.allVoted && !votingRevealed) {
            document.getElementById('reveal-btn').classList.remove('hidden');
        } else {
            document.getElementById('reveal-btn').classList.add('hidden');
        }
        if (votingRevealed) {
            document.getElementById('reset-btn').classList.remove('hidden');
        } else {
            document.getElementById('reset-btn').classList.add('hidden');
        }
    } else {
        document.getElementById('story-input-section').classList.add('hidden');
        document.getElementById('reveal-btn').classList.add('hidden');
        document.getElementById('reset-btn').classList.add('hidden');
    }
}

function renderPlayers(players) {
    const container = document.getElementById('players-list');
    container.innerHTML = '';
    
    if (!players || players.length === 0) {
        container.innerHTML = '<p style="color: #94a3b8;">Henüz oyuncu yok</p>';
        return;
    }
    
    console.log('Oyuncular render ediliyor:', players);
    
    // Aynı isimdeki oyuncuları tespit et
    const nameCounts = {};
    players.forEach(p => {
        if (p && p.name) {
            nameCounts[p.name] = (nameCounts[p.name] || 0) + 1;
        }
    });
    
    players.forEach(player => {
        if (!player || !player.name) {
            console.warn('Geçersiz player:', player);
            return;
        }
        
        const div = document.createElement('div');
        div.className = 'player-item';
        
        // Eğer aynı isimde birden fazla oyuncu varsa, ID'nin son 4 karakterini ekle
        let displayName = player.name;
        if (nameCounts[player.name] > 1 && player.id) {
            const shortId = player.id.substring(Math.max(0, player.id.length - 4));
            displayName = `${player.name} (${shortId})`;
        }
        
        let hostBadge = player.host ? '<span class="host-badge">HOST</span>' : '';
        let status = player.hasVoted ? 
            '<span class="player-status voted">✓ Oyladı</span>' : 
            '<span class="player-status waiting">⏳ Bekliyor</span>';
        
        // Eğer bu benim player'ım ise özel işaretle
        let isMe = player.id === currentPlayerId ? ' <strong style="color: #667eea;">(Siz)</strong>' : '';
        
        div.innerHTML = `
            <span class="player-name">${displayName}${isMe}${hostBadge}</span>
            ${status}
        `;
        container.appendChild(div);
    });
}

function renderCards() {
    const container = document.getElementById('cards-container');
    container.innerHTML = '';
    
    cards.forEach(card => {
        const div = document.createElement('div');
        div.className = 'card';
        div.textContent = card;
        div.onclick = () => vote(card);
        container.appendChild(div);
    });
}

function showResults(data) {
    const players = data.players || [];
    
    // Ortalama puanı hesapla
    const numericVotes = [];
    players.forEach(player => {
        if (player.currentVote) {
            const vote = player.currentVote;
            if (vote !== '?' && vote !== '☕') {
                const numValue = parseFloat(vote);
                if (!isNaN(numValue)) {
                    numericVotes.push(numValue);
                }
            }
        }
    });
    
    // Ortalama hesapla
    let average = 0;
    let averageText = '-';
    let detailsText = '';
    
    if (numericVotes.length > 0) {
        average = numericVotes.reduce((a, b) => a + b, 0) / numericVotes.length;
        averageText = average.toFixed(1);
        
        const fibSequence = [0, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89];
        const closestFib = fibSequence.reduce((prev, curr) => 
            Math.abs(curr - average) < Math.abs(prev - average) ? curr : prev
        );
        
        detailsText = `${numericVotes.length} oyuncu oy verdi • En yakın Fibonacci: ${closestFib}`;
    } else {
        averageText = 'N/A';
        detailsText = 'Sayısal oy yok';
    }
    
    // Ortalamayı göster
    const averageSection = document.getElementById('average-section');
    averageSection.classList.remove('hidden');
    document.getElementById('average-value').textContent = averageText;
    document.getElementById('average-details').textContent = detailsText;
    
    // Detaylı sonuçları göster
    const resultsSection = document.getElementById('results-section');
    resultsSection.classList.remove('hidden');
    
    const container = document.getElementById('results-container');
    container.innerHTML = '';
    
    const voteResults = data.voteResults || {};
    
    // Aynı isimdeki oyuncuları tespit et
    const nameCounts = {};
    players.forEach(p => {
        nameCounts[p.name] = (nameCounts[p.name] || 0) + 1;
    });
    
    // Oy sayılarını göster
    let resultsHtml = '<div class="results-summary">';
    Object.entries(voteResults)
        .sort((a, b) => b[1] - a[1])
        .forEach(([card, count]) => {
            resultsHtml += `
                <div class="result-item">
                    <span class="result-card">${card}</span>
                    <span class="result-count">${count} oy</span>
                </div>
            `;
        });
    resultsHtml += '</div>';
    
    // Oyuncu oylarını göster
    resultsHtml += '<div class="player-votes"><h4>Oyuncu Oyları:</h4>';
    players.forEach(player => {
        if (player.currentVote) {
            let displayName = player.name;
            if (nameCounts[player.name] > 1) {
                const shortId = player.id.substring(player.id.length - 4);
                displayName = `${player.name} (${shortId})`;
            }
            
            resultsHtml += `
                <div class="player-vote-item">
                    <span>${displayName}</span>
                    <span style="font-weight:bold">${player.currentVote}</span>
                </div>
            `;
        }
    });
    resultsHtml += '</div>';
    
    container.innerHTML = resultsHtml;
}

function resetUI() {
    document.getElementById('average-section').classList.add('hidden');
    document.getElementById('results-section').classList.add('hidden');
    document.querySelectorAll('.card').forEach(card => card.classList.remove('selected'));
    document.getElementById('selected-card').classList.add('hidden');
    document.getElementById('story-input').value = '';
}

function showWelcomeScreen() {
    document.getElementById('welcome-screen').classList.add('active');
    document.getElementById('game-screen').classList.remove('active');
    document.getElementById('join-form').classList.add('hidden');
    currentPlayerId = null;
    currentRoomId = null;
    currentPlayerName = null;
    isHost = false;
    votingRevealed = false;
    stompClient = null;
}

function showGameScreen() {
    document.getElementById('welcome-screen').classList.remove('active');
    document.getElementById('game-screen').classList.add('active');
}
