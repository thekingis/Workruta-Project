//
//  NetworkManager.swift
//  Unslpash
//
//  Created by Sam Meech-Ward on 2020-05-25.
//  Copyright © 2020 Sam Meech-Ward. All rights reserved.
//

import Foundation

enum NetworkManagerError: Error {
  case badResponse(URLResponse?)
  case badData
  case badLocalUrl
}
