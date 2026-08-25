import "./NewsList.css";

const FALLBACK_IMAGE = "https://placehold.co/900x560/172033/E8EDF6?text=Newsroom";

function formatDate(value) {
  if (!value || Number.isNaN(new Date(value).getTime())) return "Date unavailable";
  return new Intl.DateTimeFormat(undefined, { dateStyle: "medium", timeStyle: "short" }).format(new Date(value));
}

function readableText(value, fallback) {
  if (!value) return fallback;
  const element = document.createElement("textarea");
  element.innerHTML = value.replace(/<[^>]*>/g, " ");
  return element.value.replace(/\s+/g, " ").trim();
}

function sourceStyle(source) {
  return source?.toLowerCase().includes("guardian") ? "guardian" : "nyt";
}

function StoryCard({ article, featured = false }) {
  const title = article.title || "Untitled story";
  const description = readableText(article.description, "Open the original article to read the complete story.");

  return <article className={`story-card${featured ? " featured-story" : ""}`}>
    <div className="image-wrap">
      <img src={article.imageUrl || FALLBACK_IMAGE} onError={(event) => { event.currentTarget.src = FALLBACK_IMAGE; }} alt="" loading={featured ? "eager" : "lazy"} />
      {featured && <span className="lead-label">LEAD STORY</span>}
    </div>
    <div className="story-content">
      <div className="story-meta"><span className={`source-pill ${sourceStyle(article.source)}`}>{article.source || "News"}</span><time dateTime={article.publishedAt || undefined}>{formatDate(article.publishedAt)}</time></div>
      <h3><a href={article.url} target="_blank" rel="noreferrer noopener">{title}</a></h3>
      <p className="story-description">{description}</p>
      <a className="read-link" href={article.url} target="_blank" rel="noreferrer noopener" aria-label={`Read ${title} on ${article.source || "source"}`}>Read original <span aria-hidden="true">↗</span></a>
    </div>
  </article>;
}

function LoadingState() {
  return <div aria-label="Loading news">
    <div className="story-card featured-story skeleton"><div className="skeleton-image" /><div className="story-content"><i /><i /><i /><i /></div></div>
    <div className="news-grid skeleton-grid">{Array.from({ length: 5 }, (_, index) => <div className="story-card skeleton" key={index}><div className="skeleton-image" /><div className="story-content"><i /><i /><i /></div></div>)}</div>
  </div>;
}

export default function NewsList({ articles, loading, error, onRetry }) {
  if (error) return <section className="state-card error-state"><span aria-hidden="true">!</span><p className="state-label">REQUEST FAILED</p><h3>We lost the news signal</h3><p>{error}</p><button type="button" onClick={onRetry}>Try again <span aria-hidden="true">→</span></button></section>;
  if (loading) return <LoadingState />;
  if (!articles.length) return <section className="state-card"><span aria-hidden="true">⌕</span><p className="state-label">NO RESULTS</p><h3>No matching stories</h3><p>Try a broader topic, another company name, or check your spelling.</p></section>;

  const [leadStory, ...otherStories] = articles;
  return <div className="news-list"><StoryCard article={leadStory} featured />{otherStories.length > 0 && <div className="news-grid">{otherStories.map((article, index) => <StoryCard article={article} key={article.url || `${article.title}-${index}`} />)}</div>}</div>;
}
