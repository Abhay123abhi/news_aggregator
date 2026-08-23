import "./NewsList.css";

const FALLBACK_IMAGE = "https://placehold.co/800x480/182235/E8EDF6?text=Newsroom";
function formatDate(value) { if (!value || Number.isNaN(new Date(value).getTime())) return "Date unavailable"; return new Intl.DateTimeFormat(undefined, { dateStyle: "medium" }).format(new Date(value)); }
function StoryCard({ article }) { return <article className="story-card"><div className="image-wrap"><img src={article.imageUrl || FALLBACK_IMAGE} onError={(event) => { event.currentTarget.src = FALLBACK_IMAGE; }} alt="" loading="lazy" /><span className="source-pill">{article.source || "News"}</span></div><div className="story-content"><p className="story-date">{formatDate(article.publishedAt)}</p><h3>{article.title || "Untitled story"}</h3><p className="story-description">{article.description || "Open the original article to read more."}</p><a href={article.url} target="_blank" rel="noreferrer noopener" aria-label={`Read ${article.title || "article"} on ${article.source || "source"}`}>Read story <span aria-hidden="true">↗</span></a></div></article>; }
export default function NewsList({ articles, loading, error, onRetry }) {
  if (error) return <section className="state-card error-state"><span aria-hidden="true">!</span><h3>Something went wrong</h3><p>{error}</p><button type="button" onClick={onRetry}>Try again</button></section>;
  if (loading) return <div className="news-grid" aria-label="Loading news">{Array.from({ length: 6 }, (_, index) => <div className="story-card skeleton" key={index}><div className="skeleton-image" /><div className="story-content"><i /><i /><i /></div></div>)}</div>;
  if (!articles.length) return <section className="state-card"><span aria-hidden="true">⌕</span><h3>No matching stories</h3><p>Try a broader search term or check your spelling.</p></section>;
  return <div className="news-grid">{articles.map((article, index) => <StoryCard article={article} key={article.url || `${article.title}-${index}`} />)}</div>;
}
