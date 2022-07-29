//
//  EditRouteViewController.swift
//  Workruta
//
//  Created by The KING on 19/06/2022.
//

import UIKit
import SwiftUI
import GooglePlaces

class EditRouteViewController: UIViewController {
    
    @IBOutlet weak var controlView : UIView!
    public var this: PreviousRoutesViewController!
    public var routesUIView: RoutesUIView!
    public var routeIndex: Int!
    public var routeData: [String: Any]!
    private var that: EditRouteUIView!
    private var index = -1
    let autocompleteCtrller = GMSAutocompleteViewController()
    let filter = GMSAutocompleteFilter()
    let fields: GMSPlaceField = [.name, .coordinate]

    override func viewDidLoad() {
        super.viewDidLoad()
        
        if controlView != nil {
            let childView = UIHostingController(rootView: EditRouteUIView(this: self, routeData: routeData))
            addChild(childView)
            childView.view.frame = controlView.bounds
            controlView.addSubview(childView.view)
            
            filter.type = .establishment
            filter.countries = [Constants.country]
            autocompleteCtrller.delegate = self
            autocompleteCtrller.autocompleteFilter = filter
            autocompleteCtrller.placeFields = fields
            autocompleteCtrller.primaryTextColor = UIColor.black
            autocompleteCtrller.secondaryTextColor = UIColor.lightGray
            autocompleteCtrller.tableCellSeparatorColor = UIColor.lightGray
            autocompleteCtrller.tableCellBackgroundColor = UIColor.white
            UITextField.appearance(whenContainedInInstancesOf: [UISearchBar.self]).defaultTextAttributes = [NSAttributedString.Key.foregroundColor: UIColor.black]
        }

    }
    
    func editRoute(routeData: [String: Any]){
        if self.routeData["locationFrom"] as! String == routeData["locationFrom"] as! String
            && self.routeData["locationTo"] as! String == routeData["locationTo"] as! String
            && self.routeData["passNum"] as! String == routeData["passNum"] as! String
            && self.routeData["routeDate"] as! String == routeData["routeDate"] as! String
            && self.routeData["freeRide"] as! Bool == routeData["freeRide"] as! Bool {
            self.showAlertBox(title: "", msg: "No changes made", btnText: "Close")
            return
        }
        if routeData["passNum"] as! String == "" {
            self.showAlertBox(title: "", msg: "Please fill in all fields", btnText: "Close")
            return
        }
        if this != nil {
            this.saveEdit(routeData: routeData, routeIndex: routeIndex)
        }
        if routesUIView != nil {
            routesUIView.saveEdit(routeData: routeData)
        }
        self.finish()
    }
    
    func openAutoSuggest(that: EditRouteUIView, index: Int) {
        self.that = that
        self.index = index
        self.present(autocompleteCtrller, animated: true, completion: nil)
    }

}

extension EditRouteViewController: GMSAutocompleteViewControllerDelegate {
    func viewController(_ viewController: GMSAutocompleteViewController, didAutocompleteWith place: GMSPlace) {
        let address = place.name!
        let latitude = place.coordinate.latitude
        let longitude = place.coordinate.longitude
        that.setLocationValues(address: address, latitude: latitude, longitude: longitude, index: index)
        dismiss(animated: true, completion: nil)
    }
    
    func viewController(_ viewController: GMSAutocompleteViewController, didFailAutocompleteWithError error: Error) {
    }
    
    func wasCancelled(_ viewController: GMSAutocompleteViewController) {
        dismiss(animated: true, completion: nil)
    }
    
    func didRequestAutocompletePredictions(_ viewController: GMSAutocompleteViewController) {
    }
    
    func didUpdateAutocompletePredictions(_ viewController: GMSAutocompleteViewController) {
    }
}
