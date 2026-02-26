function BikeList({ bikes, loading, sortBy, onSort }) {
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
        <a
          key={bike.productUrl}
          className="bike-card"
          href={bike.productUrl}
          target="_blank"
          rel="noreferrer"
        >
          <div className="bike-card-main">
            <span className="bike-brand">{bike.brand}</span>
            <span className="bike-model">{bike.model}</span>
          </div>
          <div className="bike-card-meta">
            {bike.discipline && <span className="tag">{bike.discipline}</span>}
            {bike.price != null && <span className="price">${bike.price.toLocaleString()}</span>}
          </div>
        </a>
      ))}
    </div>
  )
}

export default BikeList
