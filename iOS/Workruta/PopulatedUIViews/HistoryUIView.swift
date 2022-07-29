//
//  HistoryUIView.swift
//  Workruta
//
//  Created by The KING on 16/06/2022.
//

import SwiftUI

struct HistoryUIView: View {
    
    let this: HistoryViewController
    @ObservedObject var routeModels: RouteModels
    let spaceName = "scroll"
    @State var dict = [String: Any]()
    @State var optionId = ""
    @State var optionType = ""
    @State var optionStatus = ""
    @State var actionText = ""
    @State var actionNeg = ""
    @State var actionPos = ""
    @State var routeIndex = -1
    @State var actionIndex = -1
    @State var showSheet = false
    @State var showAction = false
    @State var showOptions = false
    @State var wholeSize: CGSize = .zero
    @State var scrollViewSize: CGSize = .zero
    let optionIcons = ["eye", "location.fill.viewfinder", "plus.circle", "pencil", "xmark.circle.fill"]
    let optionTexts = ["View Route", "Search for Available Routes", "Recreate Route", "Edit Route", "Cancel Route"]
    
    var body: some View {
        ZStack {
            Colors.white
            if routeModels.routesArray.count > 0 {
                let c = routeModels.routesArray.count
                ChildSizeReader(size: $wholeSize) {
                    ScrollView(showsIndicators: false) {
                        ChildSizeReader(size: $scrollViewSize) {
                            VStack{
                                ForEach(0..<c, id: \.self){ index in
                                    let routeModel = getRouteModel(dict: routeModels.routesArray[index])
                                    Button {
                                        let editing = routeModel.routeArray["editing"] as! Bool
                                        if !editing {
                                            routeIndex = index
                                            dict = routeModel.routeArray
                                            optionId = routeModel.routeArray["id"] as! String
                                            optionType = routeModel.routeArray["type"] as! String
                                            optionStatus = routeModel.routeArray["status"] as! String
                                            showOptions = true
                                        }
                                    } label: {
                                        RouteBoxUIView(routeModel: routeModel)
                                    }
                                }
                                if routeModels.requesting {
                                    GIFView(gifName: "loader")
                                        .frame(width: 30, height: 30, alignment: .center)
                                        .padding(top: 30, bottom: 30)
                                }
                            }
                            .background(
                                GeometryReader { proxy in
                                    Color.clear.preference(
                                        key: ViewOffsetKey.self,
                                        value: -1 * proxy.frame(in: .named(spaceName)).origin.y
                                    )
                                }
                            )
                            .onPreferenceChange(
                                ViewOffsetKey.self,
                                perform: { value in
                                    let reachedBottom = value >= scrollViewSize.height - wholeSize.height - 50
                                    if !routeModels.requesting && !routeModels.allLoaded && reachedBottom {
                                        this.getRoutes()
                                    }
                                }
                            )
                        }
                    }
                    .coordinateSpace(name: spaceName)
                }
                .padding(top: 40)
            } else {
                if !routeModels.allLoaded {
                    GIFView(gifName: "loader")
                        .frame(width: 30, height: 30, alignment: .center)
                } else {
                    Text(Strings.no_route_created)
                        .foregroundColor(Colors.black)
                }
            }
        }
        .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .topLeading)
        .overlay(
            HStack(alignment: .center, spacing: 10){
                (Text(Image(systemName: "chevron.left")) + Text(Strings.history)).foregroundColor(Colors.white).padding(10).font(.system(size: 18)).onTapGesture {
                    this.finish()
                }
                Spacer()
            }
                .background(Colors.mainColor)
            , alignment: .topLeading
        ).overlay(
            HStack{
                if showOptions {
                    ZStack(){
                        Colors.blackFade
                    }.frame(width: UIScreen.main.bounds.width, height: UIScreen.main.bounds.height).overlay(
                        HStack{
                            Spacer()
                            VStack(spacing: 10){
                                ForEach(0...3, id: \.self){ index in
                                    let changeColor = setAction(index: index)
                                    let optionIcon = optionIcons[index]
                                    let optionText = optionTexts[index]
                                    Button {
                                        if changeColor {
                                            executeMenuOption(index: index)
                                        }
                                    } label: {
                                        HStack(spacing: 10){
                                            Image(systemName: optionIcon)
                                                .foregroundColor(changeColor ? Colors.mainColor : Colors.asher)
                                                .font(.system(size: 17))
                                            Text(optionText)
                                                .foregroundColor(changeColor ? Colors.mainColor : Colors.asher)
                                                .font(.system(size: 17))
                                            Spacer()
                                        }.frame(minWidth: 0, maxWidth: .infinity).padding(10).overlay(RoundedRectangle(cornerRadius: 7.0).stroke(Colors.asher, lineWidth: 1.5))
                                    }
                                }
                            }.padding(EdgeInsets(top: 10, leading: 10, bottom: 20, trailing: 10)).background(Colors.white).frame(width: UIScreen.main.bounds.width - 20).cornerRadius(10).overlay(RoundedRectangle(cornerRadius: 10.0).stroke(Colors.white, lineWidth: 0))
                            Spacer()
                        }.padding(EdgeInsets(top: 0, leading: 0, bottom: -10, trailing: 0))
                        , alignment: .bottomLeading
                    ).onTapGesture {
                        showOptions = false
                    }
                }
            }
            , alignment: .bottomLeading
        ).overlay(
            ZStack{
                if showAction {
                    Colors.blackFade
                    VStack (spacing: 30){
                        Text(actionText).multilineTextAlignment(.center).font(.system(size: 16)).foregroundColor(Colors.black)
                        HStack (spacing: 15){
                            Spacer()
                            Button {
                                showAction = false
                            } label: {
                                Text(actionNeg).foregroundColor(Colors.white).padding(20).background(Colors.black).cornerRadius(5)
                            }
                            Button {
                                executeOption()
                            } label: {
                                Text(actionPos).foregroundColor(Colors.white).padding(20).background(Colors.mainColor).cornerRadius(5)
                            }
                        }
                    }.frame(width: UIScreen.main.bounds.width - 60).padding(20).background(Colors.white).cornerRadius(10)
                }
            }
            , alignment: .topLeading
        ).sheet(isPresented: $showSheet) {
            RecreateRouteUIView(previousRoutesUIView: nil, historyUIView: self, createRouteUIView: nil, routeSearchUIView: nil, thisIndex: 1, routeDetails: dict).background(Colors.white)
        }
    }
    
    func checkRecreateData(passNum: String, freeRide: Bool, routeDate: Date) {
        self.showSheet.toggle()
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.1){
            this.recreateRoute(passNum: passNum, freeRide: freeRide, routeDate: routeDate, routeArray: dict)
        }
    }
    
    func executeOption() {
        showAction = false
        if actionIndex == 2 {
            showSheet.toggle()
            return
        }
    }
    
    func getRouteModel (dict: [String: Any]) -> RouteModel {
        let routeModel = RouteModel()
        routeModel.routeArray = dict
        return routeModel
    }
    
    func executeMenuOption(index: Int){
        showOptions = false
        if index == 0 {
            this.openRouteInfo(routeId: optionId)
        }
        if index == 2 {
            actionText = Strings.recreate_route
            actionNeg = Strings.cancel
            actionPos = Strings.recreate
            actionIndex = index
            showAction = true
        }
    }
    
    func setAction(index: Int) -> Bool{
        return index == 0 || index == 2
    }
}
