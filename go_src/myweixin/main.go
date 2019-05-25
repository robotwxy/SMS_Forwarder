package main

import (
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"weixinmp"
)

var mp *weixinmp.Weixinmp
var counter int = 1

func main() {

	token := "xxxxxxxxxx"            // wechat Token test
	appid := "xxxxxxxxxxxxxxxxxx"    // wechat AppID
	secret := "xxxxxxxxxxxxxxxxxxxx" // wechat AppSecret
	mp = weixinmp.New(token, appid, secret)
	mp.AccessToken.Fresh()
	http.HandleFunc("/receiver", receiver)
	http.HandleFunc("/forwarder", forwarder)
	log.Fatal(http.ListenAndServe(":80", nil))
}

type SMSData struct {
	data string
}

var msgRemain = 20

func forwarder(w http.ResponseWriter, r *http.Request) {
	raw, err := ioutil.ReadAll(r.Body)
	if err != nil {
		fmt.Println(err)
		return
	}
	fmt.Println(string(raw))
	msgRemain = msgRemain - 1
	str := fmt.Sprintf("sms: \n%s \n ************* \n%d messages can be sent\n", string(raw), msgRemain)
	str1, err := mp.SendTextMsg("xxxxxxxxxxxxxxxxxx", str) // user id
	w.Write([]byte(str1))
	counter = counter + 1
}
func receiver(w http.ResponseWriter, r *http.Request) {
	if !mp.Request.IsValid(w, r) {
		return
	}
	msgRemain = 20
	fmt.Println(mp.Request.MsgType, "from", mp.Request.FromUserName)
	fmt.Println(mp.Request.MsgType, "from", mp.Request.ToUserName)
	if mp.Request.MsgType == weixinmp.MsgTypeText {
		mp.ReplyTextMsg(w, "Hello, world")
	}
}
