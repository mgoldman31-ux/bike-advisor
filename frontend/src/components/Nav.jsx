function Nav({ activePage, onNavigate }) {
  const tabs = ['Home', 'Stats', 'Methodology']

  return (
    <nav className="nav">
      <span className="nav-wordmark">Faster That Bike</span>
      {tabs.map(tab => (
        <button
          key={tab}
          className={`nav-tab ${activePage === tab ? 'active' : ''}`}
          onClick={() => onNavigate(tab)}
        >
          {tab}
        </button>
      ))}
    </nav>
  )
}

export default Nav
