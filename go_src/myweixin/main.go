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
	/*
		token := "wxy1994812"                        // 微信公众平台的Token
		appid := "wx2691dccf0c9cd63a"                // 微信公众平台的AppID
		secret := "d5c062193ac8e546d0938b05d5bf8bdd" // 微信公众平台的AppSecret
	*/
	token := "wxytest"                           // 微信公众平台的Token test
	appid := "wx15e22c8145dc256f"                // 微信公众平台的AppID
	secret := "a2eaf380a52039236782f14b08acc1a2" // 微信公众平台的AppSecret
	// 仅被动响应消息时可不填写appid、secret
	// 仅主动发送消息时可不填写token
	mp = weixinmp.New(token, appid, secret)
	mp.AccessToken.Fresh()
	// 注册处理函数
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
	str1, err := mp.SendTextMsg("oFJmg53dfpTrGse53V0PifOE7xY0", str)
	w.Write([]byte(str1))
	counter = counter + 1
}
func receiver(w http.ResponseWriter, r *http.Request) {
	// 检查请求是否有效
	// 仅主动发送消息时不用检查
	if !mp.Request.IsValid(w, r) {
		return
	}
	msgRemain = 20
	fmt.Println(mp.Request.MsgType, "from", mp.Request.FromUserName)
	fmt.Println(mp.Request.MsgType, "from", mp.Request.ToUserName)
	// 判断消息类型
	if mp.Request.MsgType == weixinmp.MsgTypeText {
		// 回复消息
		mp.ReplyTextMsg(w, "Hello, 世界")
	}
}
