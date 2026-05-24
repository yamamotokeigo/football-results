import React, { useEffect, useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { CalendarDays, RefreshCw, Send, Shield, Trash2 } from 'lucide-react';
import './styles.css';

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8081/api';

function App() {
  const [teams, setTeams] = useState([]);
  const [matches, setMatches] = useState([]);
  const [standings, setStandings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState('');
  const [form, setForm] = useState({
    matchDate: new Date().toISOString().slice(0, 10),
    homeTeamId: '',
    awayTeamId: '',
    homeScore: 0,
    awayScore: 0,
    venue: 'Local Stadium',
  });

  const teamOptions = useMemo(() => teams.map((team) => (
    <option key={team.id} value={team.id}>{team.name}</option>
  )), [teams]);

  async function loadData() {
    setLoading(true);
    setMessage('');
    try {
      const [teamRes, matchRes, standingRes] = await Promise.all([
        fetch(`${API_BASE}/teams`),
        fetch(`${API_BASE}/matches`),
        fetch(`${API_BASE}/standings`),
      ]);

      if (!teamRes.ok || !matchRes.ok || !standingRes.ok) {
        throw new Error('API response was not successful.');
      }

      const [teamData, matchData, standingData] = await Promise.all([
        teamRes.json(),
        matchRes.json(),
        standingRes.json(),
      ]);

      setTeams(teamData);
      setMatches(matchData);
      setStandings(standingData);
      setForm((current) => ({
        ...current,
        homeTeamId: current.homeTeamId || String(teamData[0]?.id ?? ''),
        awayTeamId: current.awayTeamId || String(teamData[1]?.id ?? ''),
      }));
    } catch (error) {
      setMessage(`Could not connect to the API: ${error.message}`);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadData();
  }, []);

  function updateForm(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  }

  async function submitMatch(event) {
    event.preventDefault();
    setSubmitting(true);
    setMessage('');

    try {
      const response = await fetch(`${API_BASE}/matches`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          ...form,
          homeTeamId: Number(form.homeTeamId),
          awayTeamId: Number(form.awayTeamId),
          homeScore: Number(form.homeScore),
          awayScore: Number(form.awayScore),
        }),
      });

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message ?? 'Failed to save the match.');
      }

      setMessage('Match saved. Standings were recalculated in the same transaction.');
      await loadData();
    } catch (error) {
      setMessage(error.message);
    } finally {
      setSubmitting(false);
    }
  }

  async function deleteMatch(matchId) {
    setMessage('');
    const response = await fetch(`${API_BASE}/matches/${matchId}`, { method: 'DELETE' });
    if (response.ok) {
      setMessage('Match deleted. Standings were recalculated.');
      await loadData();
    } else {
      setMessage('Failed to delete the match.');
    }
  }

  return (
    <main className="app-shell">
      <section className="top-bar">
        <div>
          <p className="eyebrow">Spring Boot + React</p>
          <h1>Football Results</h1>
        </div>
        <button className="icon-button" onClick={loadData} title="Refresh" type="button">
          <RefreshCw size={18} />
        </button>
      </section>

      <section className="score-strip" aria-label="Recent results">
        {matches.slice(0, 3).map((match) => (
          <article className="score-tile" key={match.id}>
            <div className="match-date"><CalendarDays size={15} />{match.matchDate}</div>
            <div className="score-line">
              <span>{match.homeTeam.shortName}</span>
              <strong>{match.homeScore} - {match.awayScore}</strong>
              <span>{match.awayTeam.shortName}</span>
            </div>
            <p>{match.venue}</p>
          </article>
        ))}
      </section>

      {message && <div className="notice">{message}</div>}

      <div className="content-grid">
        <section className="panel standings-panel">
          <div className="panel-heading">
            <h2>Standings</h2>
            <span>{loading ? 'Loading' : `${standings.length} teams`}</span>
          </div>
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>#</th>
                  <th>Team</th>
                  <th>Pl</th>
                  <th>W</th>
                  <th>D</th>
                  <th>L</th>
                  <th>GD</th>
                  <th>Pts</th>
                </tr>
              </thead>
              <tbody>
                {standings.map((row) => (
                  <tr key={row.team.id}>
                    <td>{row.rank}</td>
                    <td>
                      <div className="team-cell">
                        <span>{row.team.shortName}</span>
                        {row.team.name}
                      </div>
                    </td>
                    <td>{row.played}</td>
                    <td>{row.wins}</td>
                    <td>{row.draws}</td>
                    <td>{row.losses}</td>
                    <td>{row.goalDifference > 0 ? `+${row.goalDifference}` : row.goalDifference}</td>
                    <td><strong>{row.points}</strong></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>

        <section className="panel">
          <div className="panel-heading">
            <h2>Add Result</h2>
            <Shield size={18} />
          </div>
          <form className="match-form" onSubmit={submitMatch}>
            <label>
              Date
              <input name="matchDate" type="date" value={form.matchDate} onChange={updateForm} required />
            </label>
            <label>
              Home
              <select name="homeTeamId" value={form.homeTeamId} onChange={updateForm} required>
                {teamOptions}
              </select>
            </label>
            <label>
              Away
              <select name="awayTeamId" value={form.awayTeamId} onChange={updateForm} required>
                {teamOptions}
              </select>
            </label>
            <div className="score-inputs">
              <label>
                Home
                <input name="homeScore" type="number" min="0" value={form.homeScore} onChange={updateForm} required />
              </label>
              <label>
                Away
                <input name="awayScore" type="number" min="0" value={form.awayScore} onChange={updateForm} required />
              </label>
            </div>
            <label>
              Venue
              <input name="venue" type="text" value={form.venue} onChange={updateForm} />
            </label>
            <button className="primary-button" disabled={submitting} type="submit">
              <Send size={17} />{submitting ? 'Saving' : 'Save'}
            </button>
          </form>
        </section>
      </div>

      <section className="panel full-panel">
        <div className="panel-heading">
          <h2>Match Results</h2>
          <span>{matches.length} matches</span>
        </div>
        <div className="match-list">
          {matches.map((match) => (
            <article className="match-row" key={match.id}>
              <div>
                <span className="match-date">{match.matchDate}</span>
                <strong>{match.homeTeam.name} {match.homeScore} - {match.awayScore} {match.awayTeam.name}</strong>
                <p>{match.venue}</p>
              </div>
              <button className="icon-button danger" onClick={() => deleteMatch(match.id)} title="Delete" type="button">
                <Trash2 size={17} />
              </button>
            </article>
          ))}
        </div>
      </section>

      <section className="learning-band">
        <h2>Transaction Demo</h2>
        <p>
          Spring Boot `MatchService.recordMatch` uses `@Transactional`.
          Saving a match and recalculating standings are committed together. If an exception occurs, both changes are rolled back.
        </p>
      </section>
    </main>
  );
}

createRoot(document.getElementById('root')).render(<App />);
