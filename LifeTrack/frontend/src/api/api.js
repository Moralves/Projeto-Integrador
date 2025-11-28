import axios from 'axios';

const API_URL = 'http://localhost:8080/api'; // Update with your backend API URL

export const fetchExampleData = async () => {
    try {
        const response = await axios.get(`${API_URL}/example`);
        return response.data;
    } catch (error) {
        console.error('Error fetching example data:', error);
        throw error;
    }
};

export const createExampleData = async (data) => {
    try {
        const response = await axios.post(`${API_URL}/example`, data);
        return response.data;
    } catch (error) {
        console.error('Error creating example data:', error);
        throw error;
    }
};