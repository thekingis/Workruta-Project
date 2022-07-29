//
//  RoutesViewController.swift
//  Workruta
//
//  Created by The KING on 24/07/2022.
//

import UIKit
import SwiftUI

class RoutesViewController: UIViewController {
    
    @IBOutlet weak var controlView : UIView!
    public var routeId: String!

    override func viewDidLoad() {
        super.viewDidLoad()
        
        if controlView != nil {
            let childView = UIHostingController(rootView: RoutesUIView(this: self, routeId: routeId))
            addChild(childView)
            childView.view.frame = controlView.bounds
            controlView.addSubview(childView.view)
        }

    }
    
    func openRouteInfo(_ key: String, routeIdFrom: String, routeIdTo: String){
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
            let storyboard = UIStoryboard(name: "Extras", bundle: nil)
            if key == "viewOwnRoute" {
                let viewController  = storyboard.instantiateViewController(identifier: "RoutesView") as RoutesViewController
                viewController.routeId = routeIdTo
                viewController.modalPresentationStyle = .fullScreen
                self.present(viewController, animated: true, completion: nil)
            }
            if key == "viewMergedRoute" {
                let viewController  = storyboard.instantiateViewController(identifier: "RouteView") as RouteViewController
                viewController.routeIdFrom = routeIdFrom
                viewController.routeIdTo = routeIdTo
                viewController.routeIndex = -1
                viewController.modalPresentationStyle = .fullScreen
                self.present(viewController, animated: true, completion: nil)
            }
        }
    }
    
    func openRouteInfo(routeIdFrom: String, routeIdTo: String){
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
            let storyboard = UIStoryboard(name: "Extras", bundle: nil)
            let viewController  = storyboard.instantiateViewController(identifier: "RouteView") as RouteViewController
            viewController.routeIdFrom = routeIdFrom
            viewController.routeIdTo = routeIdTo
            viewController.modalPresentationStyle = .fullScreen
            self.present(viewController, animated: true, completion: nil)
        }
    }
    
    func openEditor(that: RoutesUIView, routeData: [String: Any]){
        let storyboard = UIStoryboard(name: "Extras", bundle: nil)
        let viewController  = storyboard.instantiateViewController(identifier: "EditRouteView") as EditRouteViewController
        viewController.routeData = routeData
        viewController.routesUIView = that
        viewController.modalPresentationStyle = .fullScreen
        self.present(viewController, animated: true, completion: nil)
    }

}
