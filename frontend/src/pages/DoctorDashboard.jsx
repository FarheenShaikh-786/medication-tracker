import React, { useEffect, useState } from 'react';
import { Container, Card, Row, Col } from 'react-bootstrap';
import api from '../services/api';

const DoctorDashboard = () => {
    const [dashboardData, setDashboardData] = useState(null);

    useEffect(() => {
        fetchDashboardDetails();
    }, []);

    const fetchDashboardDetails = async () => {
        try {
            const response = await api.get('/analytics/doctor');
            setDashboardData(response.data);
        } catch (error) {
            console.error("Error fetching doctor dashboard:", error);
        }
    };

    if (!dashboardData) return <div>Loading...</div>;

    return (
        <Container className="mt-4">
            <h2 className="mb-4">Doctor Dashboard</h2>
            <Row>
                <Col md={6}>
                    <Card className="shadow mb-4 text-center">
                        <Card.Body>
                            <h4>Total Prescriptions Issued</h4>
                            <h1 className="display-4 text-primary">{dashboardData.totalPrescriptionsIssued}</h1>
                        </Card.Body>
                    </Card>
                </Col>
                <Col md={6}>
                    <Card className="shadow text-center">
                        <Card.Body>
                            <h4>Avg. Patient Adherence</h4>
                            <h1 className="display-4 text-success">{dashboardData.averagePatientAdherence}%</h1>
                        </Card.Body>
                    </Card>
                </Col>
            </Row>
        </Container>
    );
};

export default DoctorDashboard;
