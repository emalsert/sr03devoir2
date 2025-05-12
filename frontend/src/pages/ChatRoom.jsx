import React, { useState, useEffect, useRef } from 'react';
import { useParams } from 'react-router-dom';
import { Card, Form, Button, ListGroup } from 'react-bootstrap';
import { messageService, channelService } from '../services/api';

function ChatRoom() {
  const { channelId } = useParams();
  const [channel, setChannel] = useState(null);
  const [newMessage, setNewMessage] = useState('');
  const ws = useRef(null);

  useEffect(() => {
    loadChannel();

    return () => {
      if (ws.current) {
        ws.current.close();
      }
    };
  }, [channelId]);

  const loadChannel = async () => {
    try {
      const response = await channelService.getChannel(channelId);
      setChannel(response.data);
    } catch (error) {
      console.error('Error loading channel:', error);
    }
  };

  const handleSendMessage = async (e) => {
    e.preventDefault();
    if (!newMessage.trim()) return;

    try {
      await messageService.sendMessage(channelId, { content: newMessage });
      setNewMessage('');
    } catch (error) {
      console.error('Error sending message:', error);
    }
  };

  if (!channel) {
    return <div>Loading...</div>;
  }

  return (
    <Card>
      <Card.Header>
        <h3>{channel.name}</h3>
        <p className="mb-0">{channel.description}</p>
      </Card.Header>
      <Card.Body>
        <div style={{ height: '400px', overflowY: 'auto' }}>
          <p>Messages</p>
        </div>
        <Form onSubmit={handleSendMessage} className="mt-3">
          <div className="d-flex">
            <Form.Control
              type="text"
              value={newMessage}
              onChange={(e) => setNewMessage(e.target.value)}
              placeholder="Type your message..."
            />
            <Button type="submit" variant="primary" className="ms-2">
              Send
            </Button>
          </div>
        </Form>
      </Card.Body>
    </Card>
  );
}

export default ChatRoom; 