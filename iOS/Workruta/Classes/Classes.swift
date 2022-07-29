//
//  Classes.swift
//  Workruta
//
//  Created by The KING on 26/07/2022.
//

import SwiftUI
import Foundation

class DrawView: UIView {
    
    private var point = CGPoint.zero
    
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        point = (touches.first?.location(in: self))!
    }
    
    override func touchesMoved(_ touches: Set<UITouch>, with event: UIEvent?) {
        point = (touches.first?.location(in: self))!
    }
    
    public func getTouchPoint() -> CGPoint {
        return point
    }
    
}
