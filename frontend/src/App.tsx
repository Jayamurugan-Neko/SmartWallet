import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { Wallet, SplitSquareVertical, Receipt, Bell, LayoutDashboard } from 'lucide-react'
import { DashboardContent } from './components/DashboardContent'

// Layout Component
const Layout = ({ children }: { children: React.ReactNode }) => {
  return (
    <div className="flex h-screen bg-dark-900 overflow-hidden text-gray-100">
      {/* Sidebar */}
      <aside className="w-64 flex flex-col border-r border-white/5 bg-dark-950">
        <div className="p-6 flex items-center space-x-3 text-primary-500">
          <Wallet size={32} className="drop-shadow-[0_0_10px_rgba(99,102,241,0.8)]" />
          <span className="text-2xl font-bold tracking-tight text-white drop-shadow-[0_0_5px_rgba(255,255,255,0.1)]">SmartWallet</span>
        </div>
        
        <nav className="flex-1 px-4 py-4 space-y-2 mt-4">
          <NavItem icon={<LayoutDashboard size={20} />} label="Dashboard" active />
          <NavItem icon={<Receipt size={20} />} label="Transactions" />
          <NavItem icon={<SplitSquareVertical size={20} />} label="Split Bills" />
        </nav>
        
        <div className="p-4 border-t border-white/5">
          <div className="flex items-center space-x-3 p-3 bg-dark-800/80 backdrop-blur-md border border-white/5 shadow-xl rounded-2xl group cursor-pointer hover:bg-dark-700/80 transition-all">
            <div className="w-10 h-10 rounded-full bg-gradient-to-tr from-primary-500 to-accent-500 flex items-center justify-center font-bold text-white shadow-[0_0_10px_rgba(99,102,241,0.4)]">
              JD
            </div>
            <div>
              <p className="font-medium text-sm text-gray-100 group-hover:text-white transition-colors">John Doe</p>
              <p className="text-xs text-gray-400">Pro Member</p>
            </div>
          </div>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 flex flex-col h-full relative overflow-y-auto">
        <header className="h-20 flex items-center justify-between px-8 bg-dark-900/80 backdrop-blur-md sticky top-0 z-10 border-b border-white/5">
          <h1 className="text-2xl font-bold tracking-tight">Overview</h1>
          <div className="flex items-center space-x-4">
            <button className="relative p-2 rounded-full hover:bg-white/5 transition-colors focus:outline-none">
              <Bell size={24} className="text-gray-400 hover:text-white transition-colors" />
              <span className="absolute top-1 right-2 w-2 h-2 rounded-full bg-accent-500 shadow-[0_0_8px_rgba(16,185,129,0.8)]"></span>
            </button>
          </div>
        </header>
        
        <div className="p-8">
          {children}
        </div>
      </main>
    </div>
  )
}

const NavItem = ({ icon, label, active = false }: { icon: React.ReactNode, label: string, active?: boolean }) => {
  return (
    <a href="#" className={`flex items-center space-x-3 px-4 py-3 rounded-xl transition-all duration-300 ${active ? 'bg-primary-600/20 text-primary-400 border border-primary-500/20 shadow-[inset_0_0_10px_rgba(99,102,241,0.1)]' : 'text-gray-400 hover:bg-white/5 hover:text-white'}`}>
      {icon}
      <span className="font-medium">{label}</span>
    </a>
  )
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Layout><DashboardContent /></Layout>} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
