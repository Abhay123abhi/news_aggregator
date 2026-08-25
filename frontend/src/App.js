import { useCallback, useEffect, useMemo, useState } from "react";
import newsApi from "./features/news/api/newsApi";
import SearchForm from "./features/news/components/SearchForm";
import NewsList from "./features/news/components/NewsList";
import "./App.css";

const DEFAULT_QUERY = "latest";
const DEFAULT_PAGE_SIZE = 12;

function getSavedTheme() {
  const savedTheme = window.localStorage.getItem("news-theme");
  if (savedTheme) return savedTheme;
  return window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
}

export default function App() {
  const [theme, setTheme] = useState(getSavedTheme);
  const [search, setSearch] = useState({ keyword: DEFAULT_QUERY, pageSize: DEFAULT_PAGE_SIZE });
  const [data, setData] = useState({ articles: [], page: 1, totalPages: 1 });
  const [loading, setLoading] = useState(true);
  const [slowRequest, setSlowRequest] = useState(false);
  const [error, setError] = useState("");

  const loadNews = useCallback(async (keyword, page, pageSize) => {
    setLoading(true);
    setSlowRequest(false);
    setError("");
    try {
      const response = await newsApi.search(keyword, page, pageSize);
      setData(response);
      setSearch({ keyword, pageSize });
    } catch (requestError) {
      setError(
        requestError.response?.data?.detail ||
        requestError.response?.data?.message ||
        "We couldn't load the news right now. Please try again."
      );
    } finally {
      setLoading(false);
      setSlowRequest(false);
    }
  }, []);

  useEffect(() => { loadNews(DEFAULT_QUERY, 1, DEFAULT_PAGE_SIZE); }, [loadNews]);
  useEffect(() => {
    if (!loading) return undefined;
    const timer = window.setTimeout(() => setSlowRequest(true), 10000);
    return () => window.clearTimeout(timer);
  }, [loading]);
  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    window.localStorage.setItem("news-theme", theme);
  }, [theme]);

  const resultSummary = useMemo(() => {
    if (loading) return "Finding the latest stories…";
    if (!data.articles?.length) return "No stories found";
    return `${data.articles.length} stories for “${data.searchKeyword || search.keyword}”`;
  }, [data, loading, search.keyword]);

  const handleSearch = (keyword, pageSize) => loadNews(keyword, 1, pageSize);
  const handlePageChange = (page) => loadNews(search.keyword, page, search.pageSize);

  return <main className="app-shell">
    <header className="hero">
      <nav className="topbar" aria-label="Primary navigation">
        <a className="brand" href="/" aria-label="Newsroom home"><span className="brand-mark" aria-hidden="true">N</span><span>Newsroom</span></a>
        <button className="theme-toggle" type="button" onClick={() => setTheme((value) => value === "dark" ? "light" : "dark")} aria-label={`Switch to ${theme === "dark" ? "light" : "dark"} theme`}><span aria-hidden="true">{theme === "dark" ? "☀" : "☾"}</span>{theme === "dark" ? "Light" : "Dark"}</button>
      </nav>
      <div className="hero-content">
        <p className="eyebrow">YOUR DAILY BRIEFING</p><h1>Follow the stories<br />that matter.</h1>
        <p className="hero-copy">Search trusted reporting from The Guardian and The New York Times in one focused, distraction-free space.</p>
        <SearchForm initialKeyword={search.keyword} initialPageSize={search.pageSize} onSearch={handleSearch} loading={loading} />
      </div>
    </header>
    <section className="content" aria-live="polite">
      <div className="results-header"><div><p className="section-label">LATEST RESULTS</p><h2>{resultSummary}</h2></div>{data.timeTakenMs != null && !loading && <span className="speed-badge">Updated in {(data.timeTakenMs / 1000).toFixed(1)}s</span>}</div>
      {slowRequest && <div className="startup-banner" role="status">The free server is starting. Your first request can take about a minute.</div>}
      <NewsList articles={data.articles || []} loading={loading} error={error} onRetry={() => loadNews(search.keyword, data.page || 1, search.pageSize)} />
      {!loading && !error && data.articles?.length > 0 && <nav className="pagination" aria-label="News result pages"><button type="button" onClick={() => handlePageChange(data.prevPage)} disabled={!data.prevPage}>← Previous</button><span>Page {data.page || 1}</span><button type="button" onClick={() => handlePageChange(data.nextPage)} disabled={!data.nextPage}>Next →</button></nav>}
    </section>
    <footer>Newsroom aggregates public reporting. Always read the original source for full context.</footer>
  </main>;
}
