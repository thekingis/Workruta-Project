//
//  SignupFields.swift
//  Workruta
//
//  Created by The KING on 06/06/2022.
//

import Foundation

class CachedImages {
    
    private var images = NSCache<NSString, NSData>()
    
    func getImage(imageURL: URL, completion: @escaping (Data?, Error?) -> (Void)) {
      if let imageData = images.object(forKey: imageURL.absoluteString as NSString){
          print("Using cached Image")
        completion(imageData as Data, nil)
        return
      }
      
        let task = URLSession.shared.downloadTask(with: imageURL) { localUrl, response, error in
        if let error = error {
          completion(nil, error)
          return
        }
        
        guard let httpResponse = response as? HTTPURLResponse, (200...299).contains(httpResponse.statusCode) else {
          completion(nil, NetworkManagerError.badResponse(response))
          return
        }
        
        guard let localUrl = localUrl else {
          completion(nil, NetworkManagerError.badLocalUrl)
          return
        }
        
        do {
          let data = try Data(contentsOf: localUrl)
          self.images.setObject(data as NSData, forKey: imageURL.absoluteString as NSString)
          completion(data, nil)
        } catch let error {
          completion(nil, error)
        }
      }
      
      task.resume()
    }
    
}
