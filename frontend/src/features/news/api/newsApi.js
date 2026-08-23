import axios from "axios";

const client = axios.create({
  baseURL: "/api",
  timeout: 10000,
  headers: { Accept: "application/json" }
});

const newsApi = {
  async search(keyword, page, pageSize, offline = false) {
    const { data } = await client.get("/news", {
      params: { keyword: keyword.trim(), page, pageSize, offline }
    });
    return data;
  }
};

export default newsApi;
