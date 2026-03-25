import { useState, useEffect } from 'react'
import { fetchBikeDetail } from '../api/bikes'

function fmt(value, decimals = 0) {
  if (value == null) return '—'
  return decimals > 0 ? value.toFixed(decimals) : Math.round(value).toString()
}

function IndexCell({ value }) {
  if (value == null) return <td className="geo-cell geo-index">—</td>
  const opacity = (value / 100) * 0.22
  return (
    <td className="geo-cell geo-index" style={{ background: `rgba(52, 199, 89, ${opacity})` }}>
      {Math.round(value)}
    </td>
  )
}

function BikeDetail({ bike, onBack }) {
  const [rows, setRows] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchBikeDetail(bike.geometryKey)
      .then(setRows)
      .catch(console.error)
      .finally(() => setLoading(false))
  }, [bike.geometryKey])

  return (
    <div className="detail-page">
      <button className="detail-back" onClick={onBack}>← Back</button>

      <div className="detail-header">
        <div>
          <span className="detail-brand">{bike.brand}</span>
          <h1 className="detail-model">{bike.model}</h1>
        </div>
        <div className="detail-header-meta">
          {bike.discipline && <span className="tag">{bike.discipline}</span>}
          {bike.price != null && <span className="price">${bike.price.toLocaleString()}</span>}
          <a className="detail-link" href={bike.productUrl} target="_blank" rel="noreferrer">
            View on {bike.brand} ↗
          </a>
        </div>
      </div>

      {loading ? (
        <p className="status">Loading geometry...</p>
      ) : rows.length === 0 ? (
        <p className="status">No geometry data available.</p>
      ) : (
        <div className="geo-table-wrap">
          <table className="geo-table">
            <thead>
              <tr>
                <th className="geo-th">Size</th>
                <th className="geo-th">Wheel</th>
                <th className="geo-th">Reach</th>
                <th className="geo-th">Stack</th>
                <th className="geo-th">TT Eff</th>
                <th className="geo-th">HTA</th>
                <th className="geo-th">STA</th>
                <th className="geo-th">HT Len</th>
                <th className="geo-th">ST Len</th>
                <th className="geo-th">Standover</th>
                <th className="geo-th">Wheelbase</th>
                <th className="geo-th">Chainstay</th>
                <th className="geo-th">BB Drop</th>
                <th className="geo-th">Fork Off</th>
                <th className="geo-th">Trail</th>
                <th className="geo-th index-col">Stability</th>
                <th className="geo-th index-col">Aero</th>
                <th className="geo-th index-col">Agility</th>
              </tr>
            </thead>
            <tbody>
              {rows.map(row => (
                <tr key={row.sizeLabel} className="geo-row">
                  <td className="geo-cell geo-size">{row.sizeLabel}</td>
                  <td className="geo-cell">{row.wheelSize ?? '—'}</td>
                  <td className="geo-cell">{fmt(row.reach)}</td>
                  <td className="geo-cell">{fmt(row.stack)}</td>
                  <td className="geo-cell">{fmt(row.topTubeEffective)}</td>
                  <td className="geo-cell">{fmt(row.headTubeAngle, 1)}°</td>
                  <td className="geo-cell">{fmt(row.seatTubeAngleEffective, 1)}°</td>
                  <td className="geo-cell">{fmt(row.headTubeLength)}</td>
                  <td className="geo-cell">{fmt(row.seatTubeLength)}</td>
                  <td className="geo-cell">{fmt(row.standover)}</td>
                  <td className="geo-cell">{fmt(row.wheelbase)}</td>
                  <td className="geo-cell">{fmt(row.chainstay)}</td>
                  <td className="geo-cell">{fmt(row.bbDrop)}</td>
                  <td className="geo-cell">{fmt(row.forkOffset)}</td>
                  <td className="geo-cell">{fmt(row.trail)}</td>
                  <IndexCell value={row.stabilityIndex} />
                  <IndexCell value={row.aeroIndex} />
                  <IndexCell value={row.agilityIndex} />
                </tr>
              ))}
            </tbody>
          </table>
          <p className="geo-units">All measurements in mm except HTA and STA (degrees). Indexes 0–100.</p>
        </div>
      )}
    </div>
  )
}

export default BikeDetail
