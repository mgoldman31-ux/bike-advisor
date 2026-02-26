const BASE_URL = 'http://localhost:8080/api'

export async function fetchFilters() {
  const res = await fetch(`${BASE_URL}/bikes/filters`)
  if (!res.ok) throw new Error('Failed to fetch filters')
  return res.json()
}

export async function fetchScatterData() {
  const res = await fetch(`${BASE_URL}/bikes/scatter`)
  if (!res.ok) throw new Error('Failed to fetch scatter data')
  return res.json()
}

export async function fetchBikes(filters) {
  const params = new URLSearchParams()
  if (filters.brand)     params.append('brand', filters.brand)
  if (filters.discipline) params.append('discipline', filters.discipline)
  if (filters.search)    params.append('search', filters.search)
  if (filters.minPrice)  params.append('minPrice', filters.minPrice)
  if (filters.maxPrice)  params.append('maxPrice', filters.maxPrice)

  const res = await fetch(`${BASE_URL}/bikes?${params}`)
  if (!res.ok) throw new Error('Failed to fetch bikes')
  return res.json()
}
