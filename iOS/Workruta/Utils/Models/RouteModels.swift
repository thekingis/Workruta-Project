//
//  DataModels.swift
//  Workruta
//
//  Created by The KING on 08/06/2022.
//

import Foundation

class RouteModels: ObservableObject {
    @Published var requesting = false
    @Published var allLoaded = false
    @Published var routesArray: [[String: Any]] = []
}

class RouteModel: ObservableObject {
    @Published var routeArray: [String: Any] = [String: Any]()
}
