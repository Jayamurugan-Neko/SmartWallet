import { useEffect, useState } from 'react'
import { Card, CardContent } from './ui/Card'
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts'
import { Wallet, Receipt, TrendingUp, Bell } from 'lucide-react'
import { io } from 'socket.io-client'

const mockData = [
  { name: 'Mon', balance: 4000 },
  { name: 'Tue', balance: 3000 },
  { name: 'Wed', balance: 2000 },
  { name: 'Thu', balance: 2780 },
  { name: 'Fri', balance: 1890 },
  { name: 'Sat', balance: 2390 },
  { name: 'Sun', balance: 3490 },
];

export function DashboardContent() {
  const [notifications, setNotifications] = useState<string[]>([])

  // NOTE: A real implementation would connect to the STOMP endpoint of the Spring Boot 
  // notification-service (e.g., using @stomp/stompjs).
  // Here we show a placeholder for the integration pattern.

  return (
    <div className="space-y-6 animate-fade-in-up">
      {/* Cards Row */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card glass className="relative overflow-hidden">
          <CardContent className="p-6">
            <div className="absolute -top-10 -right-10 w-32 h-32 bg-primary-500/20 rounded-full blur-3xl"></div>
            <div className="flex items-center justify-between mb-2">
              <h3 className="text-gray-400 text-sm font-medium">Total Balance</h3>
              <Wallet className="text-primary-500 w-5 h-5" />
            </div>
            <p className="text-4xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-white to-gray-400">$24,562.00</p>
            <div className="mt-4 flex items-center text-accent-500 text-sm font-medium">
              <TrendingUp className="w-4 h-4 mr-1" />
              <span className="bg-accent-500/10 px-2 py-1 rounded-md">+5.2% from last month</span>
            </div>
          </CardContent>
        </Card>
        
        <Card glass>
          <CardContent className="p-6">
            <div className="flex items-center justify-between mb-2">
              <h3 className="text-gray-400 text-sm font-medium">Monthly Spending</h3>
              <Receipt className="text-gray-400 w-5 h-5" />
            </div>
            <p className="text-3xl font-bold">$3,210.45</p>
            <div className="w-full bg-dark-700 h-2 rounded-full mt-4 overflow-hidden">
              <div className="bg-gradient-to-r from-primary-500 to-indigo-400 w-[65%] h-full rounded-full shadow-[0_0_10px_rgba(99,102,241,0.5)]"></div>
            </div>
            <p className="text-xs text-gray-500 mt-2">65% of $5,000 budget</p>
          </CardContent>
        </Card>

        <Card glass className="flex flex-col justify-between">
          <CardContent className="p-6 pb-4">
            <div className="flex items-center justify-between mb-2">
              <h3 className="text-gray-400 text-sm font-medium">Upcoming Bills</h3>
              <Bell className="text-accent-500 w-5 h-5" />
            </div>
            <p className="text-3xl font-bold text-accent-400">2 Due Soon</p>
          </CardContent>
          <div className="px-6 pb-6">
            <button className="relative overflow-hidden bg-primary-600 text-white font-medium py-2 px-4 rounded-xl shadow-[0_0_15px_rgba(99,102,241,0.5)] transition-all duration-300 hover:bg-primary-500 w-full text-sm">Review Bills</button>
          </div>
        </Card>
      </div>
      
      {/* Charts/Tables Area */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <Card glass className="lg:col-span-2">
          <CardContent className="p-6 min-h-[400px]">
             <h2 className="text-lg font-bold mb-4">Cash Flow Overview</h2>
             <div className="h-[300px] w-full mt-4">
               <ResponsiveContainer width="100%" height="100%">
                 <AreaChart data={mockData} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
                   <defs>
                     <linearGradient id="colorBalance" x1="0" y1="0" x2="0" y2="1">
                       <stop offset="5%" stopColor="#6366f1" stopOpacity={0.8}/>
                       <stop offset="95%" stopColor="#6366f1" stopOpacity={0}/>
                     </linearGradient>
                   </defs>
                   <XAxis dataKey="name" stroke="#6b7280" tick={{fill: '#9ca3af'}} />
                   <YAxis stroke="#6b7280" tick={{fill: '#9ca3af'}} />
                   <CartesianGrid strokeDasharray="3 3" stroke="#374151" vertical={false} />
                   <Tooltip 
                     contentStyle={{ backgroundColor: '#1E1E1E', borderColor: '#374151', borderRadius: '0.75rem' }}
                     itemStyle={{ color: '#E5E7EB' }}
                   />
                   <Area type="monotone" dataKey="balance" stroke="#6366f1" strokeWidth={3} fillOpacity={1} fill="url(#colorBalance)" />
                 </AreaChart>
               </ResponsiveContainer>
             </div>
          </CardContent>
        </Card>
        
        <Card glass>
          <CardContent className="p-6">
            <h2 className="text-lg font-bold mb-4">Recent Transactions</h2>
            <div className="space-y-4">
              <TransactionItem name="Uber Ride" category="Transport" amount="- $24.50" date="Today, 2:45 PM" />
              <TransactionItem name="Starbucks" category="Dining" amount="- $5.40" date="Today, 9:20 AM" />
              <TransactionItem name="Salary" category="Income" amount="+ $5,240.00" date="Yesterday" isIncome />
              <TransactionItem name="Netflix" category="Entertainment" amount="- $15.99" date="Mar 24" />
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}

const TransactionItem = ({ name, category, amount, date, isIncome = false }: any) => (
  <div className="flex items-center justify-between p-3 bg-dark-700/50 backdrop-blur-sm border border-white/5 rounded-xl transition-all duration-300 hover:bg-dark-700/80 hover:shadow-2xl hover:border-white/10">
    <div className="flex items-center space-x-3">
      <div className={`w-10 h-10 rounded-xl flex items-center justify-center ${isIncome ? 'bg-accent-500/20 text-accent-400' : 'bg-dark-600 text-gray-300'}`}>
        {isIncome ? <Wallet size={18} /> : <Receipt size={18} />}
      </div>
      <div>
        <p className="font-medium text-sm text-gray-100">{name}</p>
        <p className="text-xs text-gray-500">{category} • {date}</p>
      </div>
    </div>
    <span className={`font-bold ${isIncome ? 'text-accent-400' : 'text-white'}`}>{amount}</span>
  </div>
)
