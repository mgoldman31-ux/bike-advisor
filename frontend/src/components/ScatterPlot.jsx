import {
  ResponsiveContainer, ScatterChart, Scatter,
  XAxis, YAxis, CartesianGrid, Tooltip
} from 'recharts'

const REACH_TOOLTIP = '[placeholder — your explanation of reach goes here]'
const STACK_TOOLTIP = '[placeholder — your explanation of stack goes here]'

function InfoIcon({ text }) {
  return <span className="info-icon" data-tip={text}>ⓘ</span>
}

function CustomTooltip({ active, payload }) {
  if (!active || !payload?.length) return null
  const d = payload[0].payload
  return (
    <div className="scatter-tooltip">
      <p className="scatter-tooltip-name">{d.name}</p>
      <p className="scatter-tooltip-size">Size: {d.sizeLabel}</p>
      <p className="scatter-tooltip-stat">Reach: {d.x} mm</p>
      <p className="scatter-tooltip-stat">Stack: {d.y} mm</p>
      <p className="scatter-tooltip-count">
        {d.bikeCount} {d.bikeCount === 1 ? 'bike' : 'bikes'} use this geometry
      </p>
    </div>
  )
}

function ScatterPlot({ data }) {
  const points = data.map(d => ({
    x: d.reach,
    y: d.stack,
    name: d.bikeGeometryKey,
    sizeLabel: d.sizeLabel,
    bikeCount: d.bikeCount,
  }))

  return (
    <div className="scatter-container">
      <div className="scatter-header">
        <h2 className="scatter-title">Geometry Landscape</h2>
        <p className="scatter-sub">Each dot is one frame size. Hover for details.</p>
      </div>

      <ResponsiveContainer width="100%" height={380}>
        <ScatterChart margin={{ top: 10, right: 10, bottom: 30, left: 10 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
          <XAxis
            dataKey="x"
            type="number"
            name="Reach"
            domain={['auto', 'auto']}
            tick={{ fill: 'var(--text-muted)', fontSize: 11 }}
            label={{
              value: 'Reach (mm)',
              position: 'insideBottom',
              offset: -15,
              fill: 'var(--text-muted)',
              fontSize: 12,
            }}
          />
          <YAxis
            dataKey="y"
            type="number"
            name="Stack"
            domain={['auto', 'auto']}
            tick={{ fill: 'var(--text-muted)', fontSize: 11 }}
            label={{
              value: 'Stack (mm)',
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
        <span><InfoIcon text={REACH_TOOLTIP} /> Reach</span>
        <span><InfoIcon text={STACK_TOOLTIP} /> Stack</span>
      </div>
    </div>
  )
}

export default ScatterPlot
