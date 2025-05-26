import React, { useState, useEffect, useRef } from 'react';
import { useParams } from 'react-router-dom';
import { Card, Form, Button, ListGroup, Alert, Modal } from 'react-bootstrap';
import { channelService } from '../services/api';
import { websocketService } from '../services/websocketService';

function ChatRoom() {
  const { channelId } = useParams();
  const [channel, setChannel] = useState(null);
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [error, setError] = useState(null);
  const [connectedUsers, setConnectedUsers] = useState({});
  const [showFileModal, setShowFileModal] = useState(false);
  const [selectedFile, setSelectedFile] = useState(null);
  const [uploadProgress, setUploadProgress] = useState(null);
  const fileInputRef = useRef(null);
  const messagesEndRef = useRef(null);

  useEffect(() => {
    loadChannel();
    connectWebSocket();

    return () => {
      websocketService.unsubscribeFromChannel(channelId);
      websocketService.disconnect();
    };
  }, [channelId]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const loadChannel = async () => {
    try {
      const response = await channelService.getChannel(channelId);
      setChannel(response.data);
    } catch (error) {
      console.error('Error loading channel:', error);
      setError('Failed to load channel information');
    }
  };

  const connectWebSocket = async () => {
    try {
      await websocketService.connect();
      websocketService.subscribeToChannel(channelId, (message) => {
        if (typeof message === 'string') {
          // Message simple
          setMessages(prev => [...prev, { 
            type: 'TEXT',
            content: message, 
            timestamp: new Date() 
          }]);
        } else if (message && typeof message === 'object') {
          if (message.type === 'FILE') {
            // Message avec fichier
            const fileData = websocketService.displayFile(message);
            if (fileData) {
              setMessages(prev => [...prev, {
                type: 'FILE',
                ...fileData,
                sender: message.sender || 'Unknown',
                timestamp: new Date(message.timestamp || Date.now())
              }]);
            }
          } else if (message.type === 'USER_JOINED') {
            // Mise à jour des utilisateurs connectés
            if (message.connectedUsers && typeof message.connectedUsers === 'object') {
              setConnectedUsers(message.connectedUsers);
            }
          } else if (!message.type && Object.keys(message).length > 0) {
            // Liste des utilisateurs connectés (format Map)
            setConnectedUsers(message);
          }
        }
      });
    } catch (error) {
      console.error('WebSocket connection error:', error);
      setError('Failed to connect to chat');
    }
  };

  const handleSendMessage = async (e) => {
    e.preventDefault();
    if (!newMessage.trim()) return;

    try {
      await websocketService.sendMessage(channelId, newMessage);
      setNewMessage('');
    } catch (error) {
      console.error('Error sending message:', error);
      setError('Failed to send message');
    }
  };

  const handleFileSelect = (event) => {
    const file = event.target.files[0];
    if (file) {
      try {
        websocketService.validateFile(file);
        setSelectedFile(file);
        setShowFileModal(true);
      } catch (error) {
        setError(error.message);
      }
    }
  };

  const handleFileUpload = async () => {
    if (!selectedFile) return;

    try {
      setUploadProgress('uploading');
      await websocketService.sendFile(channelId, selectedFile);
      setUploadProgress('success');
      setTimeout(() => {
        setShowFileModal(false);
        setSelectedFile(null);
        setUploadProgress(null);
      }, 1000);
    } catch (error) {
      setError(error.message);
      setUploadProgress('error');
    }
  };

  const renderMessage = (msg, index) => {
    if (!msg) return null;

    const timestamp = msg.timestamp ? new Date(msg.timestamp).toLocaleTimeString() : '';
    const sender = msg.sender || 'Unknown';

    if (msg.type === 'FILE' && msg.url) {
      return (
        <ListGroup.Item key={index} className="border-0">
          <div className="d-flex flex-column">
            <small className="text-muted">
              {sender} - {timestamp}
            </small>
            {renderFilePreview(msg)}
          </div>
        </ListGroup.Item>
      );
    }
    
    return (
      <ListGroup.Item key={index} className="border-0">
        <div className="d-flex flex-column">
          <small className="text-muted">
            {timestamp}
          </small>
          <div>{msg.content || ''}</div>
        </div>
      </ListGroup.Item>
    );
  };

  const renderFilePreview = (file) => {
    if (!file || !file.fileType) return null;

    if (file.fileType.startsWith('image/')) {
      return (
        <div className="file-preview">
          <img 
            src={file.url} 
            alt={file.fileName || 'Image'} 
            className="img-fluid rounded" 
            style={{ maxHeight: '200px' }} 
          />
          <div className="mt-2">
            <small>{file.fileName || 'Image'}</small>
          </div>
        </div>
      );
    } else if (file.fileType === 'application/pdf') {
      return (
        <div className="file-preview">
          <div className="pdf-preview p-3 bg-light rounded">
            <span>{file.fileName || 'Document PDF'}</span>
          </div>
          <a 
            href={file.url} 
            download={file.fileName || 'document.pdf'} 
            className="btn btn-sm btn-primary mt-2"
          >
            Télécharger le PDF
          </a>
        </div>
      );
    }
    return null;
  };

  if (!channel) {
    return <div>Loading...</div>;
  }

  return (
    <>
      <Card>
        <Card.Header>
          <div className="d-flex justify-content-between align-items-center">
            <div>
              <h3>{channel?.name || 'Loading...'}</h3>
              <p className="mb-0">{channel?.description || ''}</p>
            </div>
            <div>
              <small className="text-muted">
                {Object.keys(connectedUsers).length} user{Object.keys(connectedUsers).length !== 1 ? 's' : ''} online
              </small>
            </div>
          </div>
        </Card.Header>
        
        {error && (
          <Alert variant="danger" onClose={() => setError(null)} dismissible>
            {error}
          </Alert>
        )}

        <Card.Body>
          <div style={{ height: '400px', overflowY: 'auto' }} className="mb-3">
            <ListGroup variant="flush">
              {messages.map((msg, index) => renderMessage(msg, index))}
              <div ref={messagesEndRef} />
            </ListGroup>
          </div>

          <Form onSubmit={handleSendMessage}>
            <div className="d-flex">
              <Form.Control
                type="text"
                value={newMessage}
                onChange={(e) => setNewMessage(e.target.value)}
                placeholder="Type your message..."
                disabled={!websocketService.isConnected()}
              />
              <input
                type="file"
                ref={fileInputRef}
                onChange={handleFileSelect}
                style={{ display: 'none' }}
                accept=".jpg,.jpeg,.png,.gif,.pdf"
              />
              <Button
                variant="outline-secondary"
                className="ms-2"
                onClick={() => fileInputRef.current?.click()}
                disabled={!websocketService.isConnected()}
              >
                Fichier
              </Button>
              <Button 
                type="submit" 
                variant="primary" 
                className="ms-2"
                disabled={!websocketService.isConnected()}
              >
                Send
              </Button>
            </div>
          </Form>
        </Card.Body>
      </Card>

      <Modal show={showFileModal} onHide={() => setShowFileModal(false)}>
        <Modal.Header closeButton>
          <Modal.Title>Envoyer un fichier</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {selectedFile && (
            <div>
              <p><strong>Fichier sélectionné:</strong> {selectedFile.name}</p>
              <p><small>Taille: {(selectedFile.size / 1024 / 1024).toFixed(2)} MB</small></p>
              {selectedFile.type.startsWith('image/') && (
                <img 
                  src={URL.createObjectURL(selectedFile)} 
                  alt="Preview" 
                  className="img-fluid rounded mb-3" 
                  style={{ maxHeight: '200px' }}
                />
              )}
            </div>
          )}
          {uploadProgress === 'uploading' && (
            <Alert variant="info">Envoi en cours...</Alert>
          )}
          {uploadProgress === 'success' && (
            <Alert variant="success">Fichier envoyé avec succès!</Alert>
          )}
          {uploadProgress === 'error' && (
            <Alert variant="danger">Erreur lors de l'envoi du fichier</Alert>
          )}
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowFileModal(false)}>
            Annuler
          </Button>
          <Button 
            variant="primary" 
            onClick={handleFileUpload}
            disabled={uploadProgress === 'uploading'}
          >
            Envoyer
          </Button>
        </Modal.Footer>
      </Modal>
    </>
  );
}

export default ChatRoom; 