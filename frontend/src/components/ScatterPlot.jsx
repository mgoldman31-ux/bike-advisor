import {
  ResponsiveContainer, ScatterChart, Scatter,
  XAxis, YAxis, CartesianGrid, Tooltip
} from 'recharts'

const AERO_TOOLTIP = 'How aerodynamic and aggressive the riding position is — higher means more stretched out and fast.'
const STABILITY_TOOLTIP = 'How planted and confidence-inspiring the handling is — higher means more stable at speed.'

function InfoIcon({ text }) {
  return <span className="info-icon" data-tip={text}>ⓘ</span>
}

function CustomTooltip({ active, payload }) {
  if (!active || !payload?.length) return null
  const d = payload[0].payload
  return (
    <div className="scatter-tooltip">
      <p className="scatter-tooltip-name">{d.brand} {d.model}</p>
      <p className="scatter-tooltip-size">Size: {d.sizeLabel}</p>
      <p className="scatter-tooltip-stat">Stability: {Math.round(d.y)}</p>
      <p className="scatter-tooltip-stat">Aero: {Math.round(d.x)}</p>
      {d.bikeCount > 1 && (
        <p className="scatter-tooltip-count">{d.bikeCount} bikes share this geometry</p>
      )}
    </div>
  )
}

function ScatterPlot({ data }) {
  const points = data.map(d => ({
    x: d.aeroIndex,
    y: d.stabilityIndex,
    brand: d.brand,
    model: d.model,
    sizeLabel: d.sizeLabel,
    bikeCount: d.bikeCount,
  }))

  return (
    <div className="scatter-container">
      <div className="scatter-header">
        <h2 className="scatter-title">Ride Character Landscape</h2>
        <p className="scatter-sub">Each dot is one frame size. Hover for details.</p>
      </div>

      <ResponsiveContainer width="100%" height={380}>
        <ScatterChart margin={{ top: 10, right: 10, bottom: 30, left: 10 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
          <XAxis
            dataKey="x"
            type="number"
            name="Aero"
            domain={[0, 100]}
            tick={{ fill: 'var(--text-muted)', fontSize: 11 }}
            label={{
              value: 'Aero Index',
              position: 'insideBottom',
              offset: -15,
              fill: 'var(--text-muted)',
              fontSize: 12,
            }}
          />
          <YAxis
            dataKey="y"
            type="number"
            name="Stability"
            domain={[0, 100]}
            tick={{ fill: 'var(--text-muted)', fontSize: 11 }}
            label={{
              value: 'Stability Index',
              angle: -90,
              position: 'insideLeft',
              offset: 15,
              fill: 'var(--text-muted)',
              fontSize: 12,
            }}
          />
          <Tooltip content={<CustomTooltip />} cursor={{ strokeDasharray: '3 3', stroke: 'var(--border-hover)' }} />
          <Scatter
            data={points}
            fill="var(--green)"
            fillOpacity={0.6}
            stroke="var(--green)"
            strokeWidth={1}
          />
        </ScatterChart>
      </ResponsiveContainer>

      <div className="scatter-axis-info">
        <span><InfoIcon text={AERO_TOOLTIP} /> Aero Index</span>
        <span><InfoIcon text={STABILITY_TOOLTIP} /> Stability Index</span>
      </div>
    </div>
  )
}

export default ScatterPlot
