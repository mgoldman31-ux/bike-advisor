function IndexBar({ label, value }) {
  return (
    <div className="index-bar-item">
      <span className="index-bar-label">{label}</span>
      <div className="index-bar-track">
        <div className="index-bar-fill" style={{ width: `${Math.round(value)}%` }} />
      </div>
      <span className="index-bar-value">{Math.round(value)}</span>
    </div>
  )
}

function BikeList({ bikes, loading, sortBy, onSort, onSelectBike }) {
  if (loading) return <p className="status">Loading...</p>
  if (bikes.length === 0) return <p className="status">No bikes found.</p>

  return (
    <div className="bike-list">
      <div className="list-header">
        <span className="result-count">{bikes.length} bikes found</span>
        <select
          className="sort-select"
          value={sortBy ?? ''}
          onChange={e => onSort(e.target.value || null)}
        >
          <option value="">Sort: Default</option>
          <option value="price-asc">Price: Low to High</option>
        </select>
      </div>
      {bikes.map(bike => (
        <div
          key={bike.productUrl}
          className="bike-card"
          role="button"
          onClick={() => onSelectBike(bike)}
        >
          <div className="bike-card-top">
            <div className="bike-card-main">
              <span className="bike-brand">{bike.brand}</span>
              <span className="bike-model">{bike.model}</span>
            </div>
            <div className="bike-card-meta">
              {bike.discipline && <span className="tag">{bike.discipline}</span>}
              {bike.price != null && <span className="price">${bike.price.toLocaleString()}</span>}
            </div>
          </div>
          {bike.stabilityIndex != null && (
            <div className="bike-indexes">
              <IndexBar label="Stability" value={bike.stabilityIndex} />
              <IndexBar label="Aero" value={bike.aeroIndex} />
              <IndexBar label="Agility" value={bike.agilityIndex} />
            </div>
          )}
        </div>
      ))}
    </div>
  )
}

export default BikeList
