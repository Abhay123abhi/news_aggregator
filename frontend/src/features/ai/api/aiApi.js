import axios from "axios";

const client = axios.create({
  baseURL: "/api/ai",
  timeout: 90000,
  headers: { Accept: "application/json", "Content-Type": "application/json" }
});

const aiApi = {
  async status() {
    const { data } = await client.get("/status");
    return data;
  },
  async brief(articles) {
    const { data } = await client.post("/brief", { articles });
    return data;
  },
  async compare(articles) {
    const { data } = await client.post("/compare", { articles });
    return data;
  },
  async ask(question, articles) {
    const { data } = await client.post("/ask", { question, articles });
    return data;
  }
};

export default aiApi;
