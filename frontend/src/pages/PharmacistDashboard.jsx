import React, { useEffect, useState } from 'react';
import { Container, Card, Row, Col, Table } from 'react-bootstrap';
import api from '../services/api';

const PharmacistDashboard = () => {
    const [dashboardData, setDashboardData] = useState(null);
    const [inventory, setInventory] = useState([]);

    useEffect(() => {
        fetchDashboardDetails();
        fetchInventory();
    }, []);

    const fetchDashboardDetails = async () => {
        try {
            const response = await api.get('/analytics/pharmacist');
            setDashboardData(response.data);
        } catch (error) {
            console.error("Error fetching pharmacist dashboard:", error);
        }
    };

    const fetchInventory = async () => {
        try {
            const response = await api.get('/inventory');
            setInventory(response.data);
        } catch (error) {
            console.error("Error fetching inventory:", error);
        }
    };

    if (!dashboardData) return <div>Loading...</div>;

    const topSelling = Object.entries(dashboardData.topSellingMedicines || {});

    return (
        <Container className="mt-4">
            <h2 className="mb-4">Pharmacist Dashboard</h2>
            <Row>
                <Col md={6}>
                    <Card className="shadow mb-4 border-warning">
                        <Card.Body>
                            <h4>Low Stock Alerts</h4>
                            <h1 className="display-4 text-warning">{dashboardData.lowStockCount}</h1>
                        </Card.Body>
                    </Card>
                </Col>
                <Col md={6}>
                    <Card className="shadow mb-4 border-danger">
                        <Card.Body>
                            <h4>Expired Medicines</h4>
                            <h1 className="display-4 text-danger">{dashboardData.expiredMedicinesCount}</h1>
                        </Card.Body>
                    </Card>
                </Col>
            </Row>

            <Row>
                <Col md={6}>
                    <Card className="shadow">
                        <Card.Body>
                            <h4>Top Selling Medicines</h4>
                            <Table striped bordered>
                                <thead>
                                    <tr><th>Medicine</th><th>Sales</th></tr>
                                </thead>
                                <tbody>
                                    {topSelling.map(([name, val]) => (
                                        <tr key={name}><td>{name}</td><td>{val}</td></tr>
                                    ))}
                                </tbody>
                            </Table>
                        </Card.Body>
                    </Card>
                </Col>
                <Col md={6}>
                    <Card className="shadow">
                        <Card.Body>
                            <h4>Current Inventory Catalog</h4>
                            <div style={{ maxHeight: '300px', overflowY: 'auto' }}>
                                <Table striped bordered size="sm">
                                    <thead>
                                        <tr><th>Medicine</th><th>Stock</th><th>Expiry</th></tr>
                                    </thead>
                                    <tbody>
                                        {inventory.map((item) => (
                                            <tr key={item.id}>
                                                <td>{item.medicineName}</td>
                                                <td>{item.stockQuantity}</td>
                                                <td>{item.expiryDate}</td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </Table>
                            </div>
                        </Card.Body>
                    </Card>
                </Col>
            </Row>
        </Container>
    );
};

export default PharmacistDashboard;
