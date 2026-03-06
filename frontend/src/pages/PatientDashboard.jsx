import React, { useEffect, useState } from 'react';
import { Container, Card, Row, Col, Table } from 'react-bootstrap';
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js';
import { Pie } from 'react-chartjs-2';
import api from '../services/api';

ChartJS.register(ArcElement, Tooltip, Legend);

const PatientDashboard = () => {
    const [dashboardData, setDashboardData] = useState(null);
    const [prescriptions, setPrescriptions] = useState([]);

    useEffect(() => {
        fetchDashboardDetails();
        fetchPrescriptions();
    }, []);

    const fetchDashboardDetails = async () => {
        try {
            const response = await api.get('/analytics/patient');
            setDashboardData(response.data);
        } catch (error) {
            console.error("Error fetching patient dashboard:", error);
        }
    };

    const fetchPrescriptions = async () => {
        try {
            const response = await api.get('/prescriptions/patient');
            setPrescriptions(response.data);
        } catch (error) {
            console.error("Error fetching prescriptions:", error);
        }
    };

    const downloadPdf = async (fileName) => {
        try {
            const response = await api.get(`/prescriptions/download/${fileName}`, { responseType: 'blob' });
            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', fileName);
            document.body.appendChild(link);
            link.click();
        } catch (error) {
            console.error("Error downloading PDF:", error);
        }
    };

    if (!dashboardData) return <div>Loading...</div>;

    const chartData = {
        labels: ['Doses Taken', 'Doses Missed'],
        datasets: [
            {
                data: [dashboardData.totalMedicinesTaken, dashboardData.totalMedicinesMissed],
                backgroundColor: ['rgba(75, 192, 192, 0.6)', 'rgba(255, 99, 132, 0.6)'],
                borderColor: ['rgba(75, 192, 192, 1)', 'rgba(255, 99, 132, 1)'],
                borderWidth: 1,
            },
        ],
    };

    return (
        <Container className="mt-4">
            <h2 className="mb-4">Patient Dashboard</h2>
            <Row>
                <Col md={6}>
                    <Card className="shadow mb-4">
                        <Card.Body>
                            <h4>Adherence Overview</h4>
                            <div style={{ width: '60%', margin: 'auto' }}>
                                <Pie data={chartData} />
                            </div>
                        </Card.Body>
                    </Card>
                </Col>
                <Col md={6}>
                    <Card className="shadow">
                        <Card.Body>
                            <h4>My Prescriptions</h4>
                            <Table striped bordered hover>
                                <thead>
                                    <tr>
                                        <th>Doctor</th>
                                        <th>Diagnosis</th>
                                        <th>Action</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {prescriptions.map((p) => (
                                        <tr key={p.id}>
                                            <td>{p.doctorName}</td>
                                            <td>{p.diagnosis}</td>
                                            <td>
                                                {p.pdfPath && (
                                                    <button className="btn btn-sm btn-primary" onClick={() => downloadPdf(p.pdfPath.split('/').pop())}>
                                                        Download PDF
                                                    </button>
                                                )}
                                            </td>
                                        </tr>
                                    ))}
                                    {prescriptions.length === 0 && (
                                        <tr><td colSpan="3" className="text-center">No prescriptions available</td></tr>
                                    )}
                                </tbody>
                            </Table>
                        </Card.Body>
                    </Card>
                </Col>
            </Row>
        </Container>
    );
};

export default PatientDashboard;
