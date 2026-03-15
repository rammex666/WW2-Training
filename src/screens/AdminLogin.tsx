import React, { useState } from 'react';
import { Lock, X, AlertCircle } from 'lucide-react';

interface AdminLoginProps {
  onLogin: () => void;
  onClose: () => void;
}

const AdminLogin: React.FC<AdminLoginProps> = ({ onLogin, onClose }) => {
  const [password, setPassword] = useState('');
  const [error, setError] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (password === 'admin123') { // Mot de passe par défaut
      onLogin();
    } else {
      setError(true);
      setPassword('');
    }
  };

  return (
    <div className="admin-modal-overlay">
      <div className="admin-modal">
        <button className="close-btn" onClick={onClose}><X size={20} /></button>
        <Lock size={40} color="var(--gold)" className="lock-icon" />
        <h2>ACCÈS RESTREINT</h2>
        <p>Veuillez saisir le code d'accès officier</p>

        <form onSubmit={handleSubmit}>
          <input 
            type="password" 
            value={password} 
            onChange={(e) => { setPassword(e.target.value); setError(false); }}
            placeholder="Code secret"
            autoFocus
          />
          {error && <div className="error-msg"><AlertCircle size={14} /> Code incorrect</div>}
          <button type="submit" className="btn-gold">S'IDENTIFIER</button>
        </form>
      </div>

      <style>{`
        .admin-modal-overlay {
          position: fixed;
          inset: 0;
          background: rgba(0,0,0,0.85);
          display: flex;
          align-items: center;
          justify-content: center;
          z-index: 1000;
        }
        .admin-modal {
          background: var(--bg-panel);
          border: 2px solid var(--gold);
          padding: 2.5rem;
          width: 350px;
          text-align: center;
          position: relative;
        }
        .close-btn {
          position: absolute;
          right: 10px;
          top: 10px;
          background: none;
          color: var(--text-secondary);
        }
        .lock-icon { margin-bottom: 1rem; }
        h2 { color: var(--gold); margin-bottom: 0.5rem; font-size: 1.2rem; }
        p { color: var(--text-secondary); font-size: 0.9rem; margin-bottom: 1.5rem; }
        input {
          width: 100%;
          background: var(--bg-card);
          border: 1px solid var(--border);
          color: white;
          padding: 12px;
          margin-bottom: 1rem;
          text-align: center;
          font-size: 1.1rem;
          letter-spacing: 3px;
        }
        .error-msg {
          color: var(--red);
          font-size: 0.8rem;
          margin-bottom: 1rem;
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 5px;
        }
        form button { width: 100%; }
      `}</style>
    </div>
  );
};

export default AdminLogin;
