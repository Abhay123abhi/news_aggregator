import React from 'react';

export default function NewsList({ data, onPageChange }) {
  const articles = data.articles || [];

  const handlePageChange = (newPage) => {
    if (newPage >= 1 && newPage <= data.totalPages) {
      onPageChange(newPage);
    }
  };

  return (
    <div
      style={{
        maxWidth: 1200,
        margin: '0 auto',
        padding: '20px 24px',
        backgroundColor: '#f9fafb',
        minHeight: '100vh',
      }}
    >
      {/* Summary Header */}
      <div
        style={{
          backgroundColor: '#ffffff',
          border: '1px solid #e2e8f0',
          borderRadius: 12,
          padding: '14px 20px',
          marginBottom: 24,
          display: 'flex',
          flexWrap: 'wrap',
          justifyContent: 'space-between',
          alignItems: 'center',
          boxShadow: '0 2px 6px rgba(0,0,0,0.05)',
        }}
      >
        <div><b>Keyword:</b> {data.searchKeyword}</div>
        <div><b>Time Taken:</b> {data.timeTakenMs} ms</div>
        <div><b>Total Pages:</b> {data.totalPages}</div>
      </div>

      {/* Articles List */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))',
          gap: 24,
        }}
      >
        {articles.map((a, i) => (
          <div
            key={a.url || i}
            style={{
              backgroundColor: '#ffffff',
              borderRadius: 12,
              boxShadow: '0 2px 8px rgba(0,0,0,0.05)',
              border: '1px solid #e5e7eb',
              overflow: 'hidden',
              transition: 'transform 0.2s ease, box-shadow 0.2s ease',
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.transform = 'translateY(-4px)';
              e.currentTarget.style.boxShadow = '0 6px 14px rgba(0,0,0,0.08)';
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.transform = 'translateY(0)';
              e.currentTarget.style.boxShadow = '0 2px 8px rgba(0,0,0,0.05)';
            }}
          >
            {/* Image */}
            {a.imageUrl ? (
              <img
                src={a.imageUrl}
                alt={a.title}
                style={{
                  width: '100%',
                  height: 200,
                  objectFit: 'cover',
                  borderBottom: '1px solid #f1f5f9',
                }}
              />
            ) : (
              <div
                style={{
                  height: 200,
                  backgroundColor: '#f1f5f9',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: '#94a3b8',
                  fontSize: 14,
                  fontStyle: 'italic',
                }}
              >
                No Image Available
              </div>
            )}

            {/* Content */}
            <div style={{ padding: 16 }}>
              <div
                style={{
                  fontSize: 17,
                  fontWeight: 600,
                  color: '#1e293b',
                  marginBottom: 8,
                  lineHeight: 1.4,
                }}
              >
                {a.title}
              </div>

              {a.description && (
                <p
                  style={{
                    fontSize: 14,
                    color: '#475569',
                    marginBottom: 12,
                    lineHeight: 1.5,
                  }}
                >
                  {a.description.length > 120
                    ? a.description.substring(0, 120) + '...'
                    : a.description}
                </p>
              )}

              <a
                href={a.url}
                target="_blank"
                rel="noreferrer"
                style={{
                  display: 'inline-block',
                  backgroundColor: '#2563eb',
                  color: '#fff',
                  padding: '6px 12px',
                  borderRadius: 6,
                  fontSize: 14,
                  textDecoration: 'none',
                  fontWeight: 500,
                }}
              >
                Read Full Article →
              </a>

              <div
                style={{
                  fontSize: 12,
                  color: '#64748b',
                  marginTop: 10,
                  borderTop: '1px solid #f1f5f9',
                  paddingTop: 8,
                }}
              >
                <b>Source:</b> {a.source} <br />
                <b>Published:</b>{' '}
                {new Date(a.publishedAt).toLocaleDateString('en-US', {
                  year: 'numeric',
                  month: 'short',
                  day: 'numeric',
                })}
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Pagination */}
      <div
        style={{
          marginTop: 32,
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          gap: 12,
          fontSize: 15,
          color: '#334155',
        }}
      >
        <button
          onClick={() => handlePageChange(data.page - 1)}
          disabled={data.page <= 1}
          style={{
            backgroundColor: data.page <= 1 ? '#cbd5e1' : '#2563eb',
            color: '#fff',
            border: 'none',
            borderRadius: 6,
            padding: '8px 14px',
            cursor: data.page <= 1 ? 'not-allowed' : 'pointer',
            fontWeight: 500,
          }}
        >
          ← Prev
        </button>

        <span>
          <b>Page:</b> {data.page} / {data.totalPages}
        </span>

        <button
          onClick={() => handlePageChange(data.page + 1)}
          disabled={data.page >= data.totalPages}
          style={{
            backgroundColor:
              data.page >= data.totalPages ? '#cbd5e1' : '#2563eb',
            color: '#fff',
            border: 'none',
            borderRadius: 6,
            padding: '8px 14px',
            cursor: data.page >= data.totalPages ? 'not-allowed' : 'pointer',
            fontWeight: 500,
          }}
        >
          Next →
        </button>
      </div>
    </div>
  );
}
