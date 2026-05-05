
# 구독 카테고리 테이블(subscription_category)
INSERT INTO subscription_category (category_name)
VALUES 
('OTT'),
('AI'),
('Music'),
('Others');

# 구독 추가 테이블(subscription_item)
INSERT INTO `subscription_item`
(`category_id`,`item_name`) VALUES
(1,"Netflix"),
(1,"Tving"),
(1,"Disney+"),
(1,"CoupangPlay"),
(1,"Watcha"),
(1,"Laftel"),
(1,"Wave"),
(1,"AppleTv"),
(2,"ChatGpt"),
(2,"Gemini"),
(2,"Claude"),
(3,"Melon"),
(3,"AppleMusic"),
(3,"Spotify"),
(3,"YoutubeMusic"),
(3,"Flo"),
(3,"Genie"),
(3,"Vibe"),
(4,"배민클럽"),
(4,"카카오톡서랍"),
(4,"유튜브프리미엄"),
(4,"쿠팡와우"),
(4,"이모티콘플러스"),
(4,"인텔리제이"),
(4,"Icloud+"),
(4, "컬리"),
(4, "네이버멤버십"),
(4, "GoogleDrive"),
(4, "Microsoft 365 Personal"),
(4, "Notion"),
(4, "Adobe");

# 카드 테이블(card)
INSERT INTO card (card_id, card_company)
VALUES (1, '신한카드'),
(2, '국민카드'),
(3, '농협카드'),
(4, '우리카드'),
(5, '하나카드'),
(6, '삼성카드'),
(7, '현대카드'),
(8, '롯데카드');

