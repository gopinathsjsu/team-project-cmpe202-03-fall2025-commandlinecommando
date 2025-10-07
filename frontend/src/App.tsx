import { LoginForm } from './components/LoginForm'
import { ImageWithFallback } from './components/figma/ImageWithFallback'
import { ShoppingBag, BookOpen, Users, ArrowRight } from 'lucide-react'
import React from 'react'

export default function App() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-white">
      {/* Header with Logo */}
      <header className="absolute top-0 left-0 right-0 z-10 p-6">
        <div className="flex items-center space-x-3">
          <div className="w-10 h-10 bg-gradient-to-br from-[#0055A2] to-[#003d75] rounded-xl flex items-center justify-center shadow-lg">
            <ShoppingBag className="w-6 h-6 text-white" />
          </div>
          <div>
            <h1 className="text-xl text-[#0055A2] tracking-tight">CampusConnect</h1>
            <p className="text-xs text-gray-500">Campus Marketplace</p>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <div className="min-h-screen flex items-center justify-center p-6 pt-24">
        <div className="w-full max-w-7xl mx-auto">
          <div className="grid lg:grid-cols-2 gap-12 items-center">
            
            {/* Left Column - Marketing Message */}
            <div className="space-y-8 text-center lg:text-left">
              <div className="space-y-6">
                <div className="inline-flex items-center space-x-2 bg-[#E5A823]/10 text-[#B8860B] px-4 py-2 rounded-full border border-[#E5A823]/20">
                  <Users className="w-4 h-4" />
                  <span className="text-sm">Student Community</span>
                </div>
                
                <h2 className="text-4xl lg:text-5xl text-gray-900 leading-tight">
                  Buy and sell easily within your 
                  <span className="text-[#0055A2] block">campus community</span>
                </h2>
                
                <p className="text-lg text-gray-600 max-w-lg mx-auto lg:mx-0">
                  Connect with fellow students to trade textbooks, electronics, furniture, and more. 
                  Safe, convenient, and campus-exclusive.
                </p>
              </div>

              {/* Hero Image */}
              <div className="relative">
                <div className="relative rounded-2xl overflow-hidden shadow-2xl">
                  <ImageWithFallback
                    src="https://images.unsplash.com/photo-1661009540490-315d16770038?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxjb2xsZWdlJTIwc3R1ZGVudHMlMjBjYW1wdXMlMjBtYXJrZXRwbGFjZSUyMGJvb2tzfGVufDF8fHx8MTc1OTcxMTEzN3ww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral"
                    alt="Students on campus with books and laptops"
                    className="w-full h-80 object-cover"
                  />
                  {/* Gradient Overlay */}
                  <div className="absolute inset-0 bg-gradient-to-tr from-[#0055A2]/20 to-transparent"></div>
                </div>
                
                {/* Floating Feature Cards */}
                <div className="absolute -bottom-4 -left-4 bg-white rounded-xl shadow-lg p-4 border border-gray-100">
                  <div className="flex items-center space-x-3">
                    <div className="w-10 h-10 bg-green-100 rounded-lg flex items-center justify-center">
                      <BookOpen className="w-5 h-5 text-green-600" />
                    </div>
                    <div>
                      <p className="text-sm text-gray-900">Textbooks</p>
                      <p className="text-xs text-gray-500">Save up to 70%</p>
                    </div>
                  </div>
                </div>
                
                <div className="absolute -top-4 -right-4 bg-white rounded-xl shadow-lg p-4 border border-gray-100">
                  <div className="flex items-center space-x-3">
                    <div className="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center">
                      <ShoppingBag className="w-5 h-5 text-blue-600" />
                    </div>
                    <div>
                      <p className="text-sm text-gray-900">Electronics</p>
                      <p className="text-xs text-gray-500">Trusted sellers</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            {/* Right Column - Login Form */}
            <div className="flex justify-center lg:justify-end">
              <div className="w-full max-w-md">
                <LoginForm />
                
                {/* Additional CTA */}
                <div className="mt-6 text-center">
                  <p className="text-sm text-gray-500 mb-3">
                    Join thousands of SJSU students already trading safely
                  </p>
                  <div className="flex items-center justify-center space-x-2 text-[#0055A2] hover:text-[#003d75] cursor-pointer transition-colors">
                    <span className="text-sm">Learn more about safety</span>
                    <ArrowRight className="w-4 h-4" />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Background Decorative Elements */}
      <div className="fixed top-0 left-0 w-full h-full pointer-events-none overflow-hidden">
        <div className="absolute top-20 right-10 w-32 h-32 bg-[#E5A823]/5 rounded-full"></div>
        <div className="absolute bottom-20 left-10 w-48 h-48 bg-[#0055A2]/5 rounded-full"></div>
        <div className="absolute top-1/2 left-1/4 w-16 h-16 bg-[#E5A823]/10 rounded-full"></div>
      </div>
    </div>
  )
}
