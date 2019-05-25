package weixinmp

import (
	"errors"
	"fmt"
	"io/ioutil"
	"os"
	"path"
	"sync"
	"time"
)

type AccessToken struct {
	AppId     string
	AppSecret string
	TmpName   string
	LckName   string
}

var mux sync.Mutex

// get fresh access_token string
func (this *AccessToken) Fresh() (string, error) {
	if this.TmpName == "" {
		this.TmpName = this.AppId + "-accesstoken.tmp"
	}
	fi, err := os.Stat(this.TmpName)
	if err != nil && !os.IsExist(err) {
		return this.fetchAndStore()
	}
	expires := fi.ModTime().Add(time.Hour).Unix()
	if expires <= time.Now().Unix() {
		err := os.Remove(this.TmpName);
		if(err != nil){
			fmt.Println(err)
		}
		return this.fetchAndStore()
	}
	tmp, err := os.Open(this.TmpName)
	if err != nil {
		return "", err
	}
	defer tmp.Close()
	data, err := ioutil.ReadAll(tmp)
	if err != nil {
		return "", err
	}
	fmt.Println("Fresh    " + string(data))
	return string(data), nil
}

func (this *AccessToken) fetchAndStore() (string, error) {
	token, err := this.fetch()
	if err != nil {
		return "", err
	}
	fmt.Println("fetchAndStore   " + token)
	mux.Lock()
	if err := this.store(token); err != nil {
		return "", err
	}
	mux.Unlock()
	return token, nil
}

func (this *AccessToken) store(token string) error {
	path := path.Dir(this.TmpName)
	fi, err := os.Stat(path)
	if os.IsNotExist(err) {
		if err := os.MkdirAll(path, os.ModePerm); err != nil {
			return err
		}
	}
	if !fi.IsDir() {
		return errors.New("path is not a directory")
	}
	tmp, err := os.OpenFile(this.TmpName, os.O_WRONLY|os.O_CREATE, os.ModePerm)
	if err != nil {
		return err
	}
	defer tmp.Close()
	if _, err := tmp.Write([]byte(token)); err != nil {
		return err
	}
	return nil
}

func (this *AccessToken) fetch() (string, error) {
	rtn, err := get(fmt.Sprintf(
		"%stoken?grant_type=client_credential&appid=%s&secret=%s",
		UrlPrefix,
		this.AppId,
		this.AppSecret,
	))
	if err != nil {
		return "", err
	}
	return rtn.AccessToken, nil
}

func (this *AccessToken) unlock() error {
	return os.Remove(this.LckName)
}

func (this *AccessToken) lock() error {
	path := path.Dir(this.LckName)
	fi, err := os.Stat(path)
	if os.IsNotExist(err) {
		if err := os.MkdirAll(path, os.ModePerm); err != nil {
			return err
		}
	}
	if !fi.IsDir() {
		return errors.New("path is not a directory")
	}
	lck, err := os.Create(this.LckName)
	if err != nil {
		return err
	}
	lck.Close()
	return nil
}

func (this *AccessToken) locked() bool {
	_, err := os.Stat(this.LckName)
	return !os.IsNotExist(err)
}
