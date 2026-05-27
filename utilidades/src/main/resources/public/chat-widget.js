// Chat Widget - Asesor IA
// Solo se comunica con el backend Java, NUNCA contiene API keys

(function() {
    'use strict';

    let chatToggle, chatWindow, chatClose, chatInput, chatSend;
    let chatMessages, chatTyping;
    let isOpen = false;

    function init() {
        chatToggle = document.getElementById('chat-toggle');
        chatWindow = document.getElementById('chat-window');
        chatClose = document.getElementById('chat-close');
        chatInput = document.getElementById('chat-input');
        chatSend = document.getElementById('chat-send');
        chatMessages = document.getElementById('chat-messages');
        chatTyping = document.getElementById('chat-typing');

        if (!chatToggle || !chatWindow) return;

        chatToggle.addEventListener('click', abrirChat);
        chatClose.addEventListener('click', cerrarChat);
        chatSend.addEventListener('click', enviarMensaje);
        chatInput.addEventListener('keydown', function(e) {
            if (e.key === 'Enter') enviarMensaje();
        });
    }

    function abrirChat() {
        chatWindow.classList.remove('d-none');
        chatToggle.classList.add('d-none');
        isOpen = true;
        chatInput.focus();
        scrollAlFinal();
    }

    function cerrarChat() {
        chatWindow.classList.add('d-none');
        chatToggle.classList.remove('d-none');
        isOpen = false;
    }

    function getEmailUsuario() {
        try {
            const user = localStorage.getItem('user');
            if (user) {
                const parsed = JSON.parse(user);
                return parsed.email || '';
            }
        } catch(e) {}
        return '';
    }

    async function enviarMensaje() {
        const texto = chatInput.value.trim();
        if (!texto) return;

        agregarMensaje('user', texto);
        chatInput.value = '';
        chatInput.disabled = true;
        chatSend.disabled = true;

        chatTyping.classList.remove('d-none');
        scrollAlFinal();

        try {
            const respuesta = await fetch('/api/chat', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    pregunta: texto,
                    emailUsuario: getEmailUsuario()
                })
            });

            const datos = await respuesta.json();

            if (datos.respuesta) {
                agregarMensaje('bot', datos.respuesta);
            } else if (datos.error) {
                agregarMensaje('bot', 'Error: ' + datos.error);
            } else {
                agregarMensaje('bot', 'Lo siento, no pude procesar tu pregunta.');
            }
        } catch (error) {
            agregarMensaje('bot', 'Error de conexi\u00f3n. Verifica que el servidor est\u00e9 corriendo en localhost:7070.');
            console.error('Chat Widget Error:', error);
        } finally {
            chatTyping.classList.add('d-none');
            chatInput.disabled = false;
            chatSend.disabled = false;
            chatInput.focus();
            scrollAlFinal();
        }
    }

    function agregarMensaje(rol, texto) {
        const div = document.createElement('div');
        div.className = 'message ' + rol;
        div.textContent = texto;
        chatMessages.appendChild(div);
        scrollAlFinal();
    }

    function scrollAlFinal() {
        setTimeout(function() {
            chatMessages.scrollTop = chatMessages.scrollHeight;
        }, 50);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
