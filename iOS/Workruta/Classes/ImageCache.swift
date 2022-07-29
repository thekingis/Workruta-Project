//
//  ImageCache.swift
//  Workruta
//
//  Created by The KING on 15/06/2022.
//

import Foundation
import UIKit

class ImageCache: NSData, NSDiscardableContent {
    
    public var imageData: NSData!
    
    func beginContentAccess() -> Bool {
        return true
    }
    
    func endContentAccess() {
        
    }
    
    func discardContentIfPossible() {
        
    }
    
    func isContentDiscarded() -> Bool {
        return false
    }
    
}
