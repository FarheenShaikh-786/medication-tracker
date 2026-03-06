import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Navbar, Nav, Container, Button } from 'react-bootstrap';
import AuthService from '../services/auth.service';

const NavigationBar = () => {
    const navigate = useNavigate();
    const currentUser = AuthService.getCurrentUser();
    const isValid = AuthService.isTokenValid();

    const handleLogout = () => {
        AuthService.logout();
        navigate('/login');
    };

    return (
        <Navbar bg="dark" variant="dark" expand="lg">
            <Container>
                <Navbar.Brand as={Link} to="/">MediTracker</Navbar.Brand>
                <Navbar.Toggle aria-controls="basic-navbar-nav" />
                <Navbar.Collapse id="basic-navbar-nav">
                    <Nav className="me-auto">
                        {!isValid && (
                            <>
                                <Nav.Link as={Link} to="/login">Login</Nav.Link>
                                <Nav.Link as={Link} to="/register">Register</Nav.Link>
                            </>
                        )}

                        {isValid && AuthService.hasRole('PATIENT') && (
                            <Nav.Link as={Link} to="/patient-dashboard">Patient Dashboard</Nav.Link>
                        )}

                        {isValid && AuthService.hasRole('DOCTOR') && (
                            <Nav.Link as={Link} to="/doctor-dashboard">Doctor Dashboard</Nav.Link>
                        )}

                        {isValid && AuthService.hasRole('PHARMACIST') && (
                            <Nav.Link as={Link} to="/pharmacist-dashboard">Pharmacist Dashboard</Nav.Link>
                        )}

                        {isValid && AuthService.hasRole('ADMIN') && (
                            <Nav.Link as={Link} to="/admin-dashboard">Admin Dashboard</Nav.Link>
                        )}
                    </Nav>

                    {isValid && currentUser && (
                        <Navbar.Text className="me-3">
                            Signed in as: {currentUser.username}
                        </Navbar.Text>
                    )}

                    {isValid && (
                        <Button variant="outline-light" onClick={handleLogout}>Logout</Button>
                    )}
                </Navbar.Collapse>
            </Container>
        </Navbar>
    );
};

export default NavigationBar;
