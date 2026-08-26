import { useState } from "react";
import aiApi from "../api/aiApi";

export default function AiWorkspace({ articles }) {
  const [question, setQuestion] = useState("");
  const [result, setResult] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const run = async (action) => {
    if (!articles?.length) return;
    setLoading(true);
    setError("");
    try {
      const response = await action();
      setResult(response.text || "No AI response returned.");
    } catch (requestError) {
      setError(
        requestError.response?.data?.detail ||
        requestError.response?.data?.message ||
        "AI is temporarily unavailable."
      );
    } finally {
      setLoading(false);
    }
  };

  const ask = () => {
    if (!question.trim()) return;
    run(() => aiApi.ask(question.trim(), articles));
  };

  return <aside className="ai-panel" id="ai-workspace">
    <div className="ai-panel-head"><span className="spark" aria-hidden="true">✦</span><span>AI WORKSPACE</span><small>LIVE</small></div>
    <h3>Turn headlines into understanding.</h3>
    <p className="ai-intro">Grounded only in the stories currently shown in your feed.</p>

    <div className="ai-feature ai-action">
      <span>01</span>
      <div><strong>Daily AI brief</strong><p>Summarize the important developments across sources.</p><button type="button" disabled={loading || !articles?.length} onClick={() => run(() => aiApi.brief(articles))}>Create brief</button></div>
    </div>

    <div className="ai-feature ai-action">
      <span>02</span>
      <div><strong>Ask the news</strong><p>Question the retrieved stories with source-grounded answers.</p><div className="ai-question"><input value={question} onChange={(event) => setQuestion(event.target.value)} placeholder="What matters most today?" onKeyDown={(event) => event.key === "Enter" && ask()} /><button type="button" disabled={loading || !question.trim()} onClick={ask}>Ask</button></div></div>
    </div>

    <div className="ai-feature ai-action">
      <span>03</span>
      <div><strong>Compare coverage</strong><p>Compare observable emphasis across publishers without guessing bias.</p><button type="button" disabled={loading || !articles?.length} onClick={() => run(() => aiApi.compare(articles))}>Compare</button></div>
    </div>

    {(loading || result || error) && <div className="ai-result" aria-live="polite">
      {loading ? <p>Analyzing the current stories…</p> : error ? <p className="ai-error">{error}</p> : <p>{result}</p>}
    </div>}

    <div className="ai-foundation"><span aria-hidden="true">✓</span><p><strong>Source-grounded by design</strong><br />AI receives only the articles already retrieved from Guardian and NYT.</p></div>
  </aside>;
}
