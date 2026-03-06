import React, { useEffect, useState } from 'react';
import { Container, Card, Row, Col, Button } from 'react-bootstrap';
import api from '../services/api';

const AdminDashboard = () => {
    const [dashboardData, setDashboardData] = useState(null);

    useEffect(() => {
        fetchDashboardDetails();
    }, []);

    const fetchDashboardDetails = async () => {
        try {
            const response = await api.get('/analytics/admin');
            setDashboardData(response.data);
        } catch (error) {
            console.error("Error fetching admin dashboard:", error);
        }
    };

    const downloadReport = async (type) => {
        try {
            const response = await api.get(`/analytics/export/${type}`, { responseType: 'blob' });
            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', `system-report.${type}`);
            document.body.appendChild(link);
            link.click();
        } catch (error) {
            console.error(`Error downloading ${type} report:`, error);
        }
    };

    if (!dashboardData) return <div>Loading...</div>;

    return (
        <Container className="mt-4">
            <h2 className="mb-4">Admin Dashboard</h2>

            <Row className="mb-4">
                <Col md={4}>
                    <Card className="shadow text-center text-white bg-primary">
                        <Card.Body>
                            <h5>Total System Users</h5>
                            <h2>{dashboardData.totalUsers}</h2>
                        </Card.Body>
                    </Card>
                </Col>
                <Col md={4}>
                    <Card className="shadow text-center text-white bg-success">
                        <Card.Body>
                            <h5>Total Prescriptions Processed</h5>
                            <h2>{dashboardData.totalPrescriptions}</h2>
                        </Card.Body>
                    </Card>
                </Col>
                <Col md={4}>
                    <Card className="shadow text-center text-white bg-danger">
                        <Card.Body>
                            <h5>System Audits & Event Logs</h5>
                            <h2>{dashboardData.totalAlerts}</h2>
                        </Card.Body>
                    </Card>
                </Col>
            </Row>

            <Card className="shadow p-4 text-center">
                <Card.Body>
                    <h4>Export System Compliance Reports</h4>
                    <p className="text-muted">Download complete analytical aggregations across the underlying tracker persistence framework.</p>
                    <div className="d-flex justify-content-center gap-3 mt-4">
                        <Button variant="outline-primary" size="lg" onClick={() => downloadReport('csv')}>Download CSV Report</Button>
                        <Button variant="danger" size="lg" onClick={() => downloadReport('pdf')}>Download PDF Report</Button>
                    </div>
                </Card.Body>
            </Card>

        </Container>
    );
};

export default AdminDashboard;
