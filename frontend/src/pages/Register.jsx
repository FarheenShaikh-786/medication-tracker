import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Container, Form, Button, Alert, Card } from 'react-bootstrap';
import AuthService from '../services/auth.service';

const Register = () => {
    const [formData, setFormData] = useState({
        username: '',
        email: '',
        password: '',
        firstName: '',
        lastName: '',
        role: 'PATIENT',
        contactNumber: '',
        dateOfBirth: '',
        specialization: '',
        licenseNumber: '',
        pharmacyName: ''
    });
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const navigate = useNavigate();

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleRegister = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        try {
            await AuthService.register(
                formData.username,
                formData.email,
                formData.password,
                [formData.role],
                {
                    firstName: formData.firstName,
                    lastName: formData.lastName,
                    contactNumber: formData.contactNumber,
                    dateOfBirth: formData.dateOfBirth,
                    specialization: formData.specialization,
                    licenseNumber: formData.licenseNumber,
                    pharmacyName: formData.pharmacyName
                }
            );
            setSuccess('Registration successful. You can now login.');
            setTimeout(() => navigate('/login'), 2000);
        } catch (err) {
            setError(err.response?.data?.message || 'Registration failed.');
        }
    };

    return (
        <Container className="d-flex align-items-center justify-content-center mt-5 mb-5">
            <Card style={{ width: '600px' }} className="shadow">
                <Card.Body>
                    <h2 className="text-center mb-4">Register</h2>
                    {error && <Alert variant="danger">{error}</Alert>}
                    {success && <Alert variant="success">{success}</Alert>}
                    <Form onSubmit={handleRegister}>
                        <div className="row">
                            <Form.Group className="mb-3 col-md-6">
                                <Form.Label>Username</Form.Label>
                                <Form.Control name="username" type="text" onChange={handleChange} required />
                            </Form.Group>
                            <Form.Group className="mb-3 col-md-6">
                                <Form.Label>Email</Form.Label>
                                <Form.Control name="email" type="email" onChange={handleChange} required />
                            </Form.Group>
                        </div>

                        <div className="row">
                            <Form.Group className="mb-3 col-md-6">
                                <Form.Label>First Name</Form.Label>
                                <Form.Control name="firstName" type="text" onChange={handleChange} required />
                            </Form.Group>
                            <Form.Group className="mb-3 col-md-6">
                                <Form.Label>Last Name</Form.Label>
                                <Form.Control name="lastName" type="text" onChange={handleChange} required />
                            </Form.Group>
                        </div>

                        <Form.Group className="mb-3">
                            <Form.Label>Password</Form.Label>
                            <Form.Control name="password" type="password" onChange={handleChange} required />
                        </Form.Group>

                        <Form.Group className="mb-3">
                            <Form.Label>Phone Number</Form.Label>
                            <Form.Control name="contactNumber" type="text" onChange={handleChange} required />
                        </Form.Group>

                        <Form.Group className="mb-3">
                            <Form.Label>Account Type</Form.Label>
                            <Form.Select name="role" onChange={handleChange}>
                                <option value="PATIENT">Patient</option>
                                <option value="DOCTOR">Doctor</option>
                                <option value="PHARMACIST">Pharmacist</option>
                            </Form.Select>
                        </Form.Group>

                        {/* Conditional fields based on role */}
                        {formData.role === 'PATIENT' && (
                            <Form.Group className="mb-3">
                                <Form.Label>Date of Birth</Form.Label>
                                <Form.Control name="dateOfBirth" type="date" onChange={handleChange} required />
                            </Form.Group>
                        )}

                        {formData.role === 'DOCTOR' && (
                            <>
                                <Form.Group className="mb-3">
                                    <Form.Label>Specialization</Form.Label>
                                    <Form.Control name="specialization" type="text" onChange={handleChange} required />
                                </Form.Group>
                                <Form.Group className="mb-3">
                                    <Form.Label>License Number</Form.Label>
                                    <Form.Control name="licenseNumber" type="text" onChange={handleChange} required />
                                </Form.Group>
                            </>
                        )}

                        {formData.role === 'PHARMACIST' && (
                            <Form.Group className="mb-3">
                                <Form.Label>Pharmacy Name</Form.Label>
                                <Form.Control name="pharmacyName" type="text" onChange={handleChange} required />
                            </Form.Group>
                        )}

                        <Button variant="success" type="submit" className="w-100">
                            Register
                        </Button>
                    </Form>
                </Card.Body>
            </Card>
        </Container>
    );
};

export default Register;
