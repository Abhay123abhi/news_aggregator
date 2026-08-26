import { useEffect, useState } from "react";
import aiApi from "../api/aiApi";
import "./AiWorkspace.css";

function AiStatus({ enabled }) {
  return <small className={`ai-status ${enabled ? "online" : "offline"}`}>
    <span aria-hidden="true">●</span>{enabled ? "ONLINE" : "OFFLINE"}
  </small>;
}

function DisabledAiWorkspace() {
  return <aside className="ai-panel" id="ai-workspace">
    <div className="ai-panel-head"><span className="spark" aria-hidden="true">✦</span><span>AI WORKSPACE</span><AiStatus enabled={false} /></div>
    <h3>Turn headlines into understanding.</h3>
    <p className="ai-intro">AI is currently off. Your normal Guardian + NYT news experience continues unchanged.</p>
    <div className="ai-feature"><span>01</span><div><strong>Daily AI brief</strong><p>Summarize the important developments across sources.</p></div></div>
    <div className="ai-feature"><span>02</span><div><strong>Ask the news</strong><p>Question retrieved articles with source-backed answers.</p></div></div>
    <div className="ai-feature"><span>03</span><div><strong>Compare coverage</strong><p>See how publishers cover the same development.</p></div></div>
    <div className="ai-foundation"><span aria-hidden="true">✓</span><p><strong>Graceful degradation</strong><br />Set AI_ENABLED=true to bring AI back online without changing the core app.</p></div>
  </aside>;
}

export default function AiWorkspace({ articles }) {
  const [question, setQuestion] = useState("");
  const [result, setResult] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [enabled, setEnabled] = useState(false);

  useEffect(() => {
    let active = true;
    aiApi.status()
      .then((response) => active && setEnabled(Boolean(response.enabled)))
      .catch(() => active && setEnabled(false));
    return () => { active = false; };
  }, []);

  const run = async (action, { clearQuestion = false } = {}) => {
    if (!enabled || !articles?.length) return;
    setLoading(true);
    setError("");
    try {
      const response = await action();
      setResult(response.text || "No AI response returned.");
      if (clearQuestion) setQuestion("");
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
    const trimmedQuestion = question.trim();
    if (!trimmedQuestion) return;
    run(() => aiApi.ask(trimmedQuestion, articles), { clearQuestion: true });
  };

  if (!enabled) return <DisabledAiWorkspace />;

  return <aside className="ai-panel" id="ai-workspace">
    <div className="ai-panel-head"><span className="spark" aria-hidden="true">✦</span><span>AI WORKSPACE</span><AiStatus enabled /></div>
    <h3>Turn headlines into understanding.</h3>
    <p className="ai-intro">Grounded only in the stories currently shown in your feed.</p>

    <div className="ai-controls">
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
    </div>

    {(loading || result || error) && <div className="ai-result" aria-live="polite">
      {loading ? <p>Analyzing the current stories…</p> : error ? <p className="ai-error">{error}</p> : <p>{result}</p>}
    </div>}

    <div className="ai-foundation"><span aria-hidden="true">✓</span><p><strong>Source-grounded by design</strong><br />AI receives only the articles already retrieved from Guardian and NYT.</p></div>
  </aside>;
}
