//
//  ChangePhotoUIView.swift
//  Workruta
//
//  Created by The KING on 10/06/2022.
//

import SwiftUI

struct ChangePhotoUIView: View {

    let this: ChangePhotoViewController
    @ObservedObject var models: Models
    var name: String
    var isBackEnabled: Bool
    let photoUrl: URL!
    @State var sourceType: UIImagePickerController.SourceType!
    @State var showOptions = false
    @State var showImagePicker = false
    @State var blackIsShowing = false
    @State var logIsShowing = false
    @State var toShow = false
    @State var selectedImage: UIImage!
    @State var croppedImage = UIImage()
    let cacheUtil = CacheUtil()
    
    var body: some View {
        ZStack(alignment: .center){
            Colors.white
            VStack(alignment: .center, spacing: 50) {
                ZStack{
                    VStack(){
                        Image(uiImage: croppedImage).resizable().scaledToFill().clipShape(RoundedRectangle(cornerRadius: 150)).onTapGesture {
                            if !models.requesting {
                                showOptions = true
                            }
                        }
                    }.frame(width: 300, height: 300, alignment: .center).cornerRadius(150).overlay(RoundedRectangle(cornerRadius: 150.0).stroke(Colors.mainColor, lineWidth: 3.0)).backgroundImage(imageName: "default_photo", cornerRadius: 150).contentShape(Circle())
                    if models.requesting {
                        CircularProgressView(progress: models.progess).frame(width: 120, height: 120).background(Colors.whiteFade).cornerRadius(60).contentShape(Circle())
                        if models.showProgressText{
                            Text(models.progressPercent).foregroundColor(Colors.green).font(.system(size: 35))
                        }
                        if models.showProgressDone {
                            Image(systemName: "checkmark").resizable().foregroundColor(Colors.green).frame(width: 50, height: 50)
                        }
                    }
                }.frame(width: 300, height: 300, alignment: .center).cornerRadius(150).overlay(RoundedRectangle(cornerRadius: 150.0).stroke(Colors.mainColor, lineWidth: 3.0)).contentShape(Circle())
                Text(Strings.click_on_the_image_to_change_it).foregroundColor(Colors.mainColor)
            }.frame(width: UIScreen.main.bounds.width, height: UIScreen.main.bounds.height)
        }.background(Colors.white)
            .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .topLeading).overlay(
                HStack(alignment: .center){
                    if !isBackEnabled && !models.requesting && models.showPhotoNext {
                        Button {
                            this.proceedForward()
                        } label: {
                            Text(Strings.next).foregroundColor(Colors.mainColor).font(.system(size: 18)).padding(EdgeInsets(top: 15, leading: 25, bottom: 15, trailing: 25))
                        }.background(Colors.white).border(Colors.mainColor, width: 1).cornerRadius(10.0).overlay(RoundedRectangle(cornerRadius: 10.0).stroke(Colors.mainColor, lineWidth: 1.0))
                    }
                    Spacer()
                    Button {
                        if !models.requesting && models.changedPhoto {
                            this.saveImage(selectedImage: croppedImage.fixedOrientation())
                        }
                    } label: {
                        Text(Strings.save).foregroundColor(Colors.white).font(.system(size: 18)).padding(EdgeInsets(top: 15, leading: 25, bottom: 15, trailing: 25))
                    }.background(Colors.mainColor).border(Colors.white, width: 1).cornerRadius(10.0).overlay(RoundedRectangle(cornerRadius: 10.0).stroke(Colors.white, lineWidth: 1.0))
                }.padding(EdgeInsets(top: 0, leading: 10, bottom: 10, trailing: 10)).frame(width: UIScreen.main.bounds.width)
                , alignment: .bottom
            ).overlay(
                HStack(){
                    if isBackEnabled {
                        Image(systemName: "chevron.left").foregroundColor(Colors.white).font(.system(size: 18)).onTapGesture {
                            this.proceedBackward()
                        }
                    }
                    Text(name).foregroundColor(Colors.white).font(.system(size: 18)).onTapGesture {
                        if isBackEnabled {
                            this.proceedBackward()
                        }
                    }
                    Spacer()
                    if !isBackEnabled {
                        Text(Strings.logout).foregroundColor(Colors.white).font(.system(size: 18)).onTapGesture {
                            blackIsShowing = true
                        }
                    }
                }.padding(10).background(Colors.mainColor)
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
                                    Button {
                                        if !models.requesting {
                                            sourceType = .camera
                                            showOptions = false
                                            showImagePicker.toggle()
                                        }
                                    } label: {
                                        HStack(spacing: 10){
                                            Image(systemName: "camera.fill").foregroundColor(Colors.asher).font(.system(size: 17))
                                            Text("Take Photo").foregroundColor(Colors.asher).font(.system(size: 17))
                                            Spacer()
                                        }.frame(minWidth: 0, maxWidth: .infinity).padding(10).overlay(RoundedRectangle(cornerRadius: 7.0).stroke(Colors.asher, lineWidth: 1.5))
                                    }
                                    Button {
                                        if !models.requesting {
                                            sourceType = .photoLibrary
                                            showOptions = false
                                            showImagePicker.toggle()
                                        }
                                    } label: {
                                        HStack(spacing: 10){
                                            Image(systemName: "photo").foregroundColor(Colors.asher).font(.system(size: 17))
                                            Text("Choose from Gallery").foregroundColor(Colors.asher).font(.system(size: 17))
                                            Spacer()
                                        }.frame(minWidth: 0, maxWidth: .infinity).padding(10).overlay(RoundedRectangle(cornerRadius: 7.0).stroke(Colors.asher, lineWidth: 1.5))
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
                    if blackIsShowing {
                        Colors.blackFade
                        VStack (spacing: 30){
                            Text(Strings.logout_text).multilineTextAlignment(.center).font(.system(size: 16)).foregroundColor(Colors.black)
                            HStack (spacing: 15){
                                Spacer()
                                Button {
                                    blackIsShowing = false
                                } label: {
                                    Text(Strings.cancel).foregroundColor(Colors.white).padding(20).background(Colors.black).cornerRadius(5)
                                }
                                Button {
                                    logoutUser()
                                } label: {
                                    Text(Strings.logout).foregroundColor(Colors.white).padding(20).background(Colors.mainColor).cornerRadius(5)
                                }
                            }
                        }.frame(width: UIScreen.main.bounds.width - 60).padding(20).background(Colors.white).cornerRadius(10)
                    }
                }
                , alignment: .topLeading
            ).overlay(
                ZStack{
                    if logIsShowing {
                        Colors.blackFade
                        HStack (alignment: .center, spacing: 10){
                            GIFView(gifName: "loader").frame(width: 20, height: 20, alignment: .center)
                            Text(Strings.logging_out).font(.system(size: 16)).foregroundColor(Colors.asher)
                            Spacer()
                        }.frame(width: UIScreen.main.bounds.width - 60).padding(left: 30, top: 50, bottom: 50).background(Colors.white).cornerRadius(10)
                    }
                }
                , alignment: .topLeading
            ).sheet(isPresented: $showImagePicker) {
                ImagePicker(sourceType: sourceType) { image in
                    toShow = true
                    selectedImage = image.fixedOrientation()
                }
            }.sheet(isPresented: $toShow) {
                ImageCropper(this: self, uiImage: $selectedImage, toShow: $toShow)
            }.onAppear(){
                if photoUrl != nil {
                    getUserImage()
                }
            }
    }
    
    func showCroppedImage(croppedImage: UIImage){
        self.models.changedPhoto = true
        self.croppedImage = croppedImage
    }
    
    func getUserImage(){
        cacheUtil.getImage(imageURL: photoUrl) { data, error in
            if let data = data {
                croppedImage = UIImage(data: data)!
            }
        }
    }
    
    func logoutUser() {
        blackIsShowing = false
        logIsShowing = true
        this.logoutUser()
    }
    
}
