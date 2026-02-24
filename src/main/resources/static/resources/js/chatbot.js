/**
 * Chatbot Widget JavaScript
 * Handles chat UI interactions, API communication, and conversation history
 */

(function() {
  'use strict';

  // DOM elements
  let toggleBtn, closeBtn, widget, messagesContainer, input, sendBtn, typingIndicator, suggestionsContainer;
  let isOpen = false;
  let locale = 'en';

  // Session storage keys
  const STORAGE_KEY_HISTORY = 'chatbot_conversation_history';
  const STORAGE_KEY_SESSION_ID = 'chatbot_session_id';

  // API endpoint
  const API_ENDPOINT = '/api/chatbot';

  /**
   * Initialize the chatbot when DOM is ready
   */
  function init() {
    // Get DOM elements
    toggleBtn = document.getElementById('chatbot-toggle');
    closeBtn = document.getElementById('chatbot-close');
    widget = document.getElementById('chatbot-widget');
    messagesContainer = document.getElementById('chatbot-messages');
    input = document.getElementById('chatbot-input');
    sendBtn = document.getElementById('chatbot-send');
    typingIndicator = document.getElementById('chatbot-typing');
    suggestionsContainer = document.getElementById('chatbot-suggestions');

    if (!toggleBtn || !widget) {
      console.error('Chatbot elements not found');
      return;
    }

    // Get locale from HTML lang attribute
    locale = document.documentElement.lang || 'en';

    // Event listeners
    toggleBtn.addEventListener('click', toggleChat);
    closeBtn.addEventListener('click', toggleChat);
    sendBtn.addEventListener('click', handleSend);
    input.addEventListener('keypress', handleKeyPress);

    // Suggestion button event delegation
    messagesContainer.addEventListener('click', handleSuggestionClick);

    // Load conversation history
    loadConversationHistory();

    console.log('Chatbot initialized with locale:', locale);
  }

  /**
   * Toggle chat widget visibility
   */
  function toggleChat() {
    isOpen = !isOpen;

    if (isOpen) {
      widget.style.display = 'flex';
      toggleBtn.style.display = 'none';
      input.focus();
    } else {
      widget.style.display = 'none';
      toggleBtn.style.display = 'flex';
    }
  }

  /**
   * Handle send button click
   */
  function handleSend() {
    const message = input.value.trim();
    if (message) {
      sendMessage(message);
    }
  }

  /**
   * Handle Enter key press in input field
   */
  function handleKeyPress(event) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      handleSend();
    }
  }

  /**
   * Handle suggestion button clicks
   */
  function handleSuggestionClick(event) {
    const suggestionBtn = event.target.closest('.chatbot-suggestion-btn');
    if (suggestionBtn) {
      const message = suggestionBtn.textContent.trim();
      sendMessage(message);
    }
  }

  /**
   * Send a message to the chatbot API
   */
  async function sendMessage(message) {
    // Hide suggestions after first message
    if (suggestionsContainer) {
      suggestionsContainer.style.display = 'none';
    }

    // Display user message
    displayMessage(message, true);

    // Clear input and disable controls
    input.value = '';
    input.disabled = true;
    sendBtn.disabled = true;

    // Show typing indicator
    showTypingIndicator();

    try {
      // Get or create session ID
      let sessionId = sessionStorage.getItem(STORAGE_KEY_SESSION_ID);
      if (!sessionId) {
        sessionId = generateSessionId();
        sessionStorage.setItem(STORAGE_KEY_SESSION_ID, sessionId);
      }

      // Make API request
      const response = await fetch(API_ENDPOINT, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
          'Accept-Language': locale
        },
        body: JSON.stringify({
          message: message,
          sessionId: sessionId,
          locale: locale
        })
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();

      // Hide typing indicator
      hideTypingIndicator();

      // Display AI response
      if (data.response) {
        displayMessage(data.response, false);
      } else {
        throw new Error('No response from server');
      }

      // Save conversation history
      saveConversationHistory();

    } catch (error) {
      console.error('Error sending message:', error);
      hideTypingIndicator();

      // Display error message
      displayErrorMessage(getErrorMessage(error));
    } finally {
      // Re-enable controls
      input.disabled = false;
      sendBtn.disabled = false;
      input.focus();
    }
  }

  /**
   * Display a message in the chat
   */
  function displayMessage(text, isUser) {
    const messageDiv = document.createElement('div');
    messageDiv.className = `chatbot-message ${isUser ? 'chatbot-message-user' : 'chatbot-message-ai'}`;

    const bubbleDiv = document.createElement('div');
    bubbleDiv.className = 'chatbot-message-bubble';

    if (isUser) {
      // User messages: plain text
      bubbleDiv.textContent = text;
    } else {
      // AI messages: render markdown
      bubbleDiv.innerHTML = renderMarkdown(text);
    }

    messageDiv.appendChild(bubbleDiv);
    messagesContainer.appendChild(messageDiv);

    // Scroll to bottom
    scrollToBottom();
  }

  /**
   * Display an error message
   */
  function displayErrorMessage(errorText) {
    const errorDiv = document.createElement('div');
    errorDiv.className = 'chatbot-error';
    errorDiv.textContent = errorText;
    messagesContainer.appendChild(errorDiv);
    scrollToBottom();
  }

  /**
   * Get localized error message
   */
  function getErrorMessage(error) {
    // Default error messages by locale
    const errorMessages = {
      en: 'Sorry, I encountered an error. Please try again.',
      de: 'Entschuldigung, es ist ein Fehler aufgetreten. Bitte versuchen Sie es erneut.',
      es: 'Lo siento, encontré un error. Por favor, inténtelo de nuevo.',
      ko: '죄송합니다. 오류가 발생했습니다. 다시 시도해 주세요.',
      fa: 'متأسفم، با خطا مواجه شدم. لطفاً دوباره امتحان کنید.',
      pt: 'Desculpe, encontrei um erro. Por favor, tente novamente.',
      ru: 'Извините, произошла ошибка. Пожалуйста, попробуйте еще раз.',
      tr: 'Üzgünüm, bir hatayla karşılaştım. Lütfen tekrar deneyin.',
      zh: '抱歉，我遇到了错误。请重试。'
    };

    return errorMessages[locale] || errorMessages.en;
  }

  /**
   * Show typing indicator
   */
  function showTypingIndicator() {
    if (typingIndicator) {
      typingIndicator.style.display = 'flex';
      scrollToBottom();
    }
  }

  /**
   * Hide typing indicator
   */
  function hideTypingIndicator() {
    if (typingIndicator) {
      typingIndicator.style.display = 'none';
    }
  }

  /**
   * Scroll messages container to bottom
   */
  function scrollToBottom() {
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
  }

  /**
   * Render markdown to HTML using marked.js
   */
  function renderMarkdown(text) {
    // Check if marked is available
    if (typeof marked !== 'undefined') {
      try {
        return marked.parse(text);
      } catch (error) {
        console.error('Error parsing markdown:', error);
        return escapeHtml(text);
      }
    } else {
      // Fallback: basic formatting
      return formatBasicMarkdown(text);
    }
  }

  /**
   * Basic markdown formatting fallback
   */
  function formatBasicMarkdown(text) {
    let formatted = escapeHtml(text);

    // Convert line breaks
    formatted = formatted.replace(/\n/g, '<br>');

    // Bold: **text**
    formatted = formatted.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');

    // Italic: *text*
    formatted = formatted.replace(/\*([^*]+)\*/g, '<em>$1</em>');

    // Links: [text](url)
    formatted = formatted.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank">$1</a>');

    // Code: `code`
    formatted = formatted.replace(/`([^`]+)`/g, '<code>$1</code>');

    return formatted;
  }

  /**
   * Escape HTML special characters
   */
  function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
  }

  /**
   * Load conversation history from session storage
   */
  function loadConversationHistory() {
    try {
      const history = sessionStorage.getItem(STORAGE_KEY_HISTORY);
      if (history) {
        const messages = JSON.parse(history);

        // Hide suggestions if there's history
        if (messages.length > 0 && suggestionsContainer) {
          suggestionsContainer.style.display = 'none';
        }

        // Display each message
        messages.forEach(msg => {
          displayMessage(msg.text, msg.isUser);
        });
      }
    } catch (error) {
      console.error('Error loading conversation history:', error);
      // Clear corrupted history
      sessionStorage.removeItem(STORAGE_KEY_HISTORY);
    }
  }

  /**
   * Save conversation history to session storage
   */
  function saveConversationHistory() {
    try {
      const messages = [];
      const messageDivs = messagesContainer.querySelectorAll('.chatbot-message');

      messageDivs.forEach(msgDiv => {
        const isUser = msgDiv.classList.contains('chatbot-message-user');
        const bubble = msgDiv.querySelector('.chatbot-message-bubble');
        const text = isUser ? bubble.textContent : bubble.textContent;

        messages.push({
          text: text,
          isUser: isUser
        });
      });

      sessionStorage.setItem(STORAGE_KEY_HISTORY, JSON.stringify(messages));
    } catch (error) {
      console.error('Error saving conversation history:', error);
    }
  }

  /**
   * Generate a unique session ID
   */
  function generateSessionId() {
    return 'session_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
  }

  // Initialize when DOM is ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }

})();
