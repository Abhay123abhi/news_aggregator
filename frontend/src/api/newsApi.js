// src/api/newsApi.js
import axios from 'axios';

const BASE_URL = process.env.REACT_APP_API_URL || "";
const API_BASE = `${BASE_URL}/api/news`;

const newsApi = {
  async search(keyword, page, pageSize, offline) {
    const params = {
      keyword,           
      page,             
      pageSize,         
      offline            
    };
    const resp = await axios.get(API_BASE, { params });
    return resp.data;
  }
};

export default newsApi;
