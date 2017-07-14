package com.jvm_bloggers.core.social.twitter

import com.jvm_bloggers.core.blogpost_redirect.LinkGenerator
import com.jvm_bloggers.entities.blog.Blog
import com.jvm_bloggers.entities.blog.BlogType
import com.jvm_bloggers.entities.blog_post.BlogPost
import com.jvm_bloggers.entities.newsletter_issue.NewsletterIssue
import com.jvm_bloggers.utils.NowProvider
import spock.lang.Specification
import spock.lang.Subject

import static com.jvm_bloggers.entities.blog.BlogType.COMPANY
import static com.jvm_bloggers.entities.blog.BlogType.PERSONAL

class TweetContentGeneratorSpec extends Specification {

    private static final long ISSUE_NUMBER = 999L
    private static final String LINK = "http://jvm-bloggers.com/issue/$ISSUE_NUMBER"

    private final Random randomJsonId = new Random()
    private final NowProvider nowProvider = new NowProvider()
    private final LinkGenerator linkGenerator = Mock(LinkGenerator)

    @Subject
    private TweetContentGenerator contentGenerator = new TweetContentGenerator(this.linkGenerator)

    def setup() {
        linkGenerator.generateIssueLink(_) >> { args -> LINK }
    }

    def "Should generate a Tweet content with an issue number and link"() {
        given:
            NewsletterIssue issue = NewsletterIssue
                    .builder()
                    .issueNumber(ISSUE_NUMBER)
                    .heading("issue heading")
                    .blogPosts(posts())
                    .build()

        when:
            String tweetContent = contentGenerator.generateTweetContent(issue)

        then:
            tweetContent.contains(issue.issueNumber as String)
            tweetContent.contains(LINK)
    }

    def "Should add two twitter handles of personal blogs"() {
        given:
            NewsletterIssue issue = NewsletterIssue
                    .builder()
                    .issueNumber(ISSUE_NUMBER)
                    .heading("issue heading")
                    .blogPosts(posts())
                    .build()

        when:
            String tweetContent = contentGenerator.generateTweetContent(issue)

        then:
            println tweetContent
            def personal = /@personal/
            def personalBlogs = (tweetContent =~ /$personal/)
            assert personalBlogs.count == 2
    }

    def "Should add one twitter handle of company blog"() {
        given:
            NewsletterIssue issue = NewsletterIssue
                    .builder()
                    .issueNumber(ISSUE_NUMBER)
                    .heading("issue heading")
                    .blogPosts(posts())
                    .build()

        when:
            String tweetContent = contentGenerator.generateTweetContent(issue)

        then:
            println tweetContent
            def company = /@company/
            def companyBlogs = (tweetContent =~ /$company/)
            assert companyBlogs.count == 1
    }

    // Should add company twitter handle as the second on handles list
    // Should not add the second personal twitter handle if message is too long
    // Should always have java and jvm tags at the end


    private Collection<BlogPost> posts() {
        List<BlogPost> posts = new ArrayList<>()
        posts.add(blogPost(blog("@personal1", PERSONAL)))
        posts.add(blogPost(blog("@personal2", PERSONAL)))
        posts.add(blogPost(blog("@company1", COMPANY)))
        posts.add(blogPost(blog("@company2", COMPANY)))
        posts.add(blogPost(blog("@personal3", PERSONAL)))
        return posts
    }

    private BlogPost blogPost(Blog blog) {
        BlogPost
            .builder()
            .title("title")
            .url("url")
            .publishedDate(nowProvider.now())
            .blog(blog)
            .build()
    }

    private Blog blog(String twitterHandle, BlogType blogType) {
        Blog.builder()
            .jsonId(randomJsonId.nextLong())
            .author("author")
            .twitter(twitterHandle)
            .rss("rss")
            .url("url")
            .dateAdded(nowProvider.now())
            .blogType(blogType)
            .build()
    }

}
