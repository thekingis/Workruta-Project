//
//  SearchUIView.swift
//  Workruta
//
//  Created by The KING on 21/06/2022.
//

import SwiftUI

struct SearchUIView: View {
    
    let previousRoutesUIView: PreviousRoutesUIView!
    let routeSearchUIView: RouteSearchUIView!
    let routesUIView: RoutesUIView!
    let thisIndex: Int
    @Binding var routeId: String
    let myId = UserDefaults.standard.string(forKey: "myId")!
    @State var requesting = true
    @State var coordinates: [[String]]!
    @State var searchData = [[String: Any]]()
    
    var body: some View {
        ZStack{
            Colors.blackFade
            VStack(alignment: .center, spacing: 20) {
                if requesting {
                    Spacer()
                    HStack {
                        Spacer()
                        VStack {
                            GIFView(gifName: "searching")
                                .frame(width: 300, height: 225, alignment: .center)
                            Text(Strings.searching_for_matching_routes).foregroundColor(Colors.asher)
                        }
                        Spacer()
                    }
                    Spacer()
                } else {
                    if searchData.count == 0 {
                        Spacer()
                        HStack {
                            Spacer()
                            Text("No match found")
                                .foregroundColor(Colors.black)
                                .font(.system(size: 16, weight: .bold))
                            Spacer()
                        }
                        Spacer()
                    } else {
                        ScrollView {
                            VStack(spacing: 20) {
                                ForEach(0..<searchData.count, id: \.self){ index in
                                    SearchRouteUIView(previousRoutesUIView: previousRoutesUIView, routeSearchUIView: routeSearchUIView, routesUIView: routesUIView, thisIndex: thisIndex, searchData: searchData[index] , coordinates: coordinates, directly: false)
                                }
                            }
                            .padding(5)
                        }
                        .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .topLeading)
                    }
                }
            }
            .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .topLeading)
            .background(Colors.white)
            .cornerRadius(7)
            .overlay(RoundedRectangle(cornerRadius: 7.0).stroke(Colors.white, lineWidth: 0.0))
        }
        .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .topLeading)
        .padding(10)
        .onAppear(){
            searchRoutes()
        }
    }
    
    func searchRoutes(){
        guard let url = URL(string: Constants.searchRoutesUrl) else {
            print("URL not found")
            return
        }
        let parameters: [String: String] = [
            "user": myId,
            "routeId": routeId
        ]
        let datas = parameters.toQueryString
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.httpBody = datas.data(using: .utf8)!
        request.addValue("application/json", forHTTPHeaderField: "Accept")
        
        let urlSession = URLSession.shared.dataTask(with: request) { data, _, error in
            DispatchQueue.main.async {
                if error != nil {
                    print("Error")
                    return
                }
                do {
                    if let data = data {
                        //print("Data: \(String(decoding: data, as: UTF8.self))")
                        let json = try JSONSerialization.jsonObject(with: data, options: .mutableContainers) as? NSDictionary
                        if let object = json {
                            self.coordinates = object["coordinates"] as? [[String]]
                            self.searchData = object["data"] as! [[String: Any]]
                        }
                    }
                } catch let myJSONError {
                    print(myJSONError)
                }
                self.requesting = false
            }
        }
        urlSession.resume()
    }
    
}
