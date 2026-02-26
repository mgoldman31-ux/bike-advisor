function FilterBar({ filters, options, onChange }) {
  function handle(field, value) {
    onChange({ ...filters, [field]: value || null })
  }

  return (
    <div className="filter-bar">

      <div className="filter-group">
        <label>Search</label>
        <input
          type="text"
          placeholder="e.g. Tarmac, Domane..."
          value={filters.search ?? ''}
          onChange={e => handle('search', e.target.value)}
        />
      </div>

      <div className="filter-group">
        <label>Brand</label>
        <input
          type="text"
          list="brand-options"
          placeholder="All brands"
          value={filters.brand ?? ''}
          onChange={e => handle('brand', e.target.value)}
        />
        <datalist id="brand-options">
          {options.brands.map(b => <option key={b} value={b} />)}
        </datalist>
      </div>

      <div className="filter-group">
        <label>Discipline</label>
        <input
          type="text"
          list="discipline-options"
          placeholder="All disciplines"
          value={filters.discipline ?? ''}
          onChange={e => handle('discipline', e.target.value)}
        />
        <datalist id="discipline-options">
          {options.disciplines.map(d => <option key={d} value={d} />)}
        </datalist>
      </div>

      <div className="filter-group">
        <label>Price</label>
        <div className="price-range">
          <input
            type="number"
            placeholder="Min"
            value={filters.minPrice ?? ''}
            onChange={e => handle('minPrice', e.target.value)}
          />
          <span>–</span>
          <input
            type="number"
            placeholder="Max"
            value={filters.maxPrice ?? ''}
            onChange={e => handle('maxPrice', e.target.value)}
          />
        </div>
      </div>

      <button className="clear-btn" onClick={() => onChange({ brand: null, discipline: null, search: null, minPrice: null, maxPrice: null })}>
        Clear
      </button>

    </div>
  )
}

export default FilterBar
