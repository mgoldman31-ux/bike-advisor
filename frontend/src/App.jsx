import { useState, useEffect } from 'react'
import Nav from './components/Nav'
import ScatterPlot from './components/ScatterPlot'
import FilterBar from './components/FilterBar'
import BikeList from './components/BikeList'
import { fetchFilters, fetchBikes, fetchScatterData } from './api/bikes'
import './App.css'

const EMPTY_FILTERS = { brand: null, discipline: null, search: null, minPrice: null, maxPrice: null }

function App() {
  const [page, setPage] = useState('Home')
  const [filters, setFilters] = useState(EMPTY_FILTERS)
  const [options, setOptions] = useState({ brands: [], disciplines: [], wheelSizes: [] })
  const [bikes, setBikes] = useState([])
  const [scatterData, setScatterData] = useState([])
  const [sortBy, setSortBy] = useState(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    fetchFilters().then(setOptions).catch(console.error)
    fetchScatterData().then(setScatterData).catch(console.error)
  }, [])

  useEffect(() => {
    setLoading(true)
    fetchBikes(filters)
      .then(setBikes)
      .catch(console.error)
      .finally(() => setLoading(false))
  }, [filters])

  return (
    <div className="app">
      <Nav activePage={page} onNavigate={setPage} />
      <main>
        {page === 'Home' && (
          <div className="home">
            <h1 className="home-heading">Find your <span>faster</span> bike.</h1>
            <p className="home-sub">Data-driven geometry scores across every major brand.</p>
            <ScatterPlot data={scatterData} />
            <FilterBar filters={filters} options={options} onChange={setFilters} />
            <BikeList bikes={sortedBikes(bikes, sortBy)} loading={loading} sortBy={sortBy} onSort={setSortBy} />
          </div>
        )}
        {page === 'Stats' && (
          <div className="light-page">
            <div className="light-section">
              <h1 className="light-heading">Stats</h1>
              <p className="light-sub">Aggregate geometry analysis across brands and disciplines — coming soon.</p>
            </div>
          </div>
        )}
        {page === 'Methodology' && (
          <div className="light-page">
            <div className="light-section">
              <h1 className="light-heading">Methodology</h1>
              <p className="light-sub">How we score and compare bike geometry — coming soon.</p>
            </div>
          </div>
        )}
      </main>
    </div>
  )
}

function sortedBikes(bikes, sortBy) {
  if (sortBy === 'price-asc') return [...bikes].sort((a, b) => (a.price ?? Infinity) - (b.price ?? Infinity))
  return bikes
}

export default App
