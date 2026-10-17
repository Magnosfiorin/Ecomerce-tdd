import React from 'react';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { CartProvider } from './context/CartContext';
import Navbar from './components/Navbar';
import AppRoutes from './routes/AppRoutes';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <CartProvider>
          <div className="app-layout">
            <Navbar />
            <main className="main-content">
              <AppRoutes />
            </main>
            <footer className="footer">
              <p>E-Commerce TDD &copy; 2026 • Disciplina de V&amp;V • Projeto Acadêmico</p>
            </footer>
          </div>
        </CartProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}
