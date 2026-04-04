// src/api/newsApi.js
import axios from 'axios';

const API_BASE = 'http://localhost:8080/api/news';

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
